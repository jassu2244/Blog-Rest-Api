package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.exception.AccessDeniedException;
import com.jasmeet.blogapi.exception.AppException;
import com.jasmeet.blogapi.exception.BadRequestException;
import com.jasmeet.blogapi.exception.ResourceNotFoundException;
import com.jasmeet.blogapi.exception.UnauthorizedException;
import com.jasmeet.blogapi.model.role.Role;
import com.jasmeet.blogapi.model.role.RoleName;
import com.jasmeet.blogapi.model.user.Address;
import com.jasmeet.blogapi.model.user.Company;
import com.jasmeet.blogapi.model.user.Geo;
import com.jasmeet.blogapi.model.user.User;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.InfoRequest;
import com.jasmeet.blogapi.payload.UserIdentityAvailability;
import com.jasmeet.blogapi.payload.UserProfile;
import com.jasmeet.blogapi.payload.UserSummary;
import com.jasmeet.blogapi.repository.PostRepository;
import com.jasmeet.blogapi.repository.RoleRepository;
import com.jasmeet.blogapi.repository.UserRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(
			UserRepository userRepository,
			PostRepository postRepository,
			RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) {

		this.userRepository = userRepository;
		this.postRepository = postRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserSummary getCurrentUser(UserPrincipal currentUser) {

		return new UserSummary(
				currentUser.getId(),
				currentUser.getUsername(),
				currentUser.getFirstName(),
				currentUser.getLastName()
		);
	}

	@Override
	public UserIdentityAvailability checkUsernameAvailability(
			String username) {

		Boolean isAvailable =
				!userRepository.existsByUsername(username);

		return new UserIdentityAvailability(isAvailable);
	}

	@Override
	public UserIdentityAvailability checkEmailAvailability(
			String email) {

		Boolean isAvailable =
				!userRepository.existsByEmail(email);

		return new UserIdentityAvailability(isAvailable);
	}

	@Override
	public UserProfile getUserProfile(String username) {

		User user = userRepository.getUserByName(username);

		Long postCount =
				postRepository.countByCreatedBy(user.getId());

		return new UserProfile(
				user.getId(),
				user.getUsername(),
				user.getFirstName(),
				user.getLastName(),
				user.getCreatedAt(),
				user.getEmail(),
				user.getAddress(),
				user.getPhone(),
				user.getWebsite(),
				user.getCompany(),
				postCount
		);
	}

	@Override
	public User addUser(User user) {

		if (userRepository.existsByUsername(user.getUsername())) {

			ApiResponse apiResponse = new ApiResponse(
					Boolean.FALSE,
					"Username is already taken"
			);

			throw new BadRequestException(apiResponse);
		}

		if (userRepository.existsByEmail(user.getEmail())) {

			ApiResponse apiResponse = new ApiResponse(
					Boolean.FALSE,
					"Email is already taken"
			);

			throw new BadRequestException(apiResponse);
		}

		List<Role> roles = new ArrayList<>();

		Role userRole = roleRepository
				.findByName(RoleName.ROLE_USER)
				.orElseThrow(
						() -> new AppException(
								"User role not set"
						)
				);

		roles.add(userRole);

		user.setRoles(roles);

		user.setPassword(
				passwordEncoder.encode(user.getPassword())
		);

		return userRepository.save(user);
	}

	@Override
	public User updateUser(
			User newUser,
			String username,
			UserPrincipal currentUser) {

		User user =
				userRepository.getUserByName(username);

		boolean isOwner =
				user.getId().equals(currentUser.getId());

		boolean isAdmin =
				currentUser.getAuthorities()
						.contains(
								new SimpleGrantedAuthority(
										RoleName.ROLE_ADMIN.toString()
								)
						);

		if (isOwner || isAdmin) {

			user.setFirstName(newUser.getFirstName());
			user.setLastName(newUser.getLastName());

			/*
			 * Only update the password when a new password
			 * has actually been provided.
			 */
			if (newUser.getPassword() != null
					&& !newUser.getPassword().isBlank()) {

				user.setPassword(
						passwordEncoder.encode(
								newUser.getPassword()
						)
				);
			}

			user.setAddress(newUser.getAddress());
			user.setPhone(newUser.getPhone());
			user.setWebsite(newUser.getWebsite());
			user.setCompany(newUser.getCompany());

			return userRepository.save(user);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to update profile of: "
						+ username
		);

		throw new UnauthorizedException(apiResponse);
	}

	@Override
	public ApiResponse deleteUser(
			String username,
			UserPrincipal currentUser) {

		User user = userRepository
				.findByUsername(username)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								"User",
								"username",
								username
						)
				);

		boolean isOwner =
				user.getId().equals(currentUser.getId());

		boolean isAdmin =
				currentUser.getAuthorities()
						.contains(
								new SimpleGrantedAuthority(
										RoleName.ROLE_ADMIN.toString()
								)
						);

		/*
		 * User can delete their own account OR an admin
		 * can delete another user's account.
		 */
		if (!isOwner && !isAdmin) {

			ApiResponse apiResponse = new ApiResponse(
					Boolean.FALSE,
					"You don't have permission to delete profile of: "
							+ username
			);

			throw new AccessDeniedException(apiResponse);
		}

		userRepository.deleteById(user.getId());

		return new ApiResponse(
				Boolean.TRUE,
				"You successfully deleted profile of: "
						+ username
		);
	}

	@Override
	public ApiResponse giveAdmin(String username) {

		User user =
				userRepository.getUserByName(username);

		List<Role> roles = new ArrayList<>();

		Role adminRole = roleRepository
				.findByName(RoleName.ROLE_ADMIN)
				.orElseThrow(
						() -> new AppException(
								"Admin role not set"
						)
				);

		Role userRole = roleRepository
				.findByName(RoleName.ROLE_USER)
				.orElseThrow(
						() -> new AppException(
								"User role not set"
						)
				);

		roles.add(adminRole);
		roles.add(userRole);

		user.setRoles(roles);

		userRepository.save(user);

		return new ApiResponse(
				Boolean.TRUE,
				"You gave ADMIN role to user: "
						+ username
		);
	}

	@Override
	public ApiResponse removeAdmin(String username) {

		User user =
				userRepository.getUserByName(username);

		Role userRole = roleRepository
				.findByName(RoleName.ROLE_USER)
				.orElseThrow(
						() -> new AppException(
								"User role not set"
						)
				);

		List<Role> roles = new ArrayList<>();
		roles.add(userRole);

		user.setRoles(roles);

		userRepository.save(user);

		return new ApiResponse(
				Boolean.TRUE,
				"You took ADMIN role from user: "
						+ username
		);
	}

	@Override
	public UserProfile setOrUpdateInfo(
			UserPrincipal currentUser,
			InfoRequest infoRequest) {

		User user = userRepository
				.findByUsername(currentUser.getUsername())
				.orElseThrow(
						() -> new ResourceNotFoundException(
								"User",
								"username",
								currentUser.getUsername()
						)
				);

		boolean isOwner =
				user.getId().equals(currentUser.getId());

		boolean isAdmin =
				currentUser.getAuthorities()
						.contains(
								new SimpleGrantedAuthority(
										RoleName.ROLE_ADMIN.toString()
								)
						);

		if (isOwner || isAdmin) {

			Geo geo = new Geo(
					infoRequest.getLat(),
					infoRequest.getLng()
			);

			Address address = new Address(
					infoRequest.getStreet(),
					infoRequest.getSuite(),
					infoRequest.getCity(),
					infoRequest.getZipcode(),
					geo
			);

			Company company = new Company(
					infoRequest.getCompanyName(),
					infoRequest.getCatchPhrase(),
					infoRequest.getBs()
			);

			user.setAddress(address);
			user.setCompany(company);
			user.setWebsite(infoRequest.getWebsite());
			user.setPhone(infoRequest.getPhone());

			User updatedUser =
					userRepository.save(user);

			Long postCount =
					postRepository.countByCreatedBy(
							updatedUser.getId()
					);

			return new UserProfile(
					updatedUser.getId(),
					updatedUser.getUsername(),
					updatedUser.getFirstName(),
					updatedUser.getLastName(),
					updatedUser.getCreatedAt(),
					updatedUser.getEmail(),
					updatedUser.getAddress(),
					updatedUser.getPhone(),
					updatedUser.getWebsite(),
					updatedUser.getCompany(),
					postCount
			);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to update users profile",
				HttpStatus.FORBIDDEN
		);

		throw new AccessDeniedException(apiResponse);
	}
}