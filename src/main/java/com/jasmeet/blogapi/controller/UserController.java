package com.jasmeet.blogapi.controller;

import com.jasmeet.blogapi.model.Album;
import com.jasmeet.blogapi.model.Post;
import com.jasmeet.blogapi.model.user.User;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.InfoRequest;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.payload.UserIdentityAvailability;
import com.jasmeet.blogapi.payload.UserProfile;
import com.jasmeet.blogapi.payload.UserSummary;
import com.jasmeet.blogapi.security.CurrentUser;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.AlbumService;
import com.jasmeet.blogapi.service.PostService;
import com.jasmeet.blogapi.service.UserService;
import com.jasmeet.blogapi.utils.AppConstants;
import com.jasmeet.blogapi.utils.AppUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final PostService postService;
	private final AlbumService albumService;

	public UserController(
			UserService userService,
			PostService postService,
			AlbumService albumService) {

		this.userService = userService;
		this.postService = postService;
		this.albumService = albumService;
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<UserSummary> getCurrentUser(
			@CurrentUser UserPrincipal currentUser) {

		UserSummary userSummary =
				userService.getCurrentUser(currentUser);

		return new ResponseEntity<>(
				userSummary,
				HttpStatus.OK
		);
	}

	@GetMapping("/checkUsernameAvailability")
	public ResponseEntity<UserIdentityAvailability> checkUsernameAvailability(
			@RequestParam(value = "username") String username) {

		UserIdentityAvailability userIdentityAvailability =
				userService.checkUsernameAvailability(username);

		return new ResponseEntity<>(
				userIdentityAvailability,
				HttpStatus.OK
		);
	}

	@GetMapping("/checkEmailAvailability")
	public ResponseEntity<UserIdentityAvailability> checkEmailAvailability(
			@RequestParam(value = "email") String email) {

		UserIdentityAvailability userIdentityAvailability =
				userService.checkEmailAvailability(email);

		return new ResponseEntity<>(
				userIdentityAvailability,
				HttpStatus.OK
		);
	}

	@GetMapping("/{username}/profile")
	public ResponseEntity<UserProfile> getUserProfile(
			@PathVariable(value = "username") String username) {

		UserProfile userProfile =
				userService.getUserProfile(username);

		return new ResponseEntity<>(
				userProfile,
				HttpStatus.OK
		);
	}

	@GetMapping("/{username}/posts")
	public ResponseEntity<PagedResponse<Post>> getPostsCreatedBy(
			@PathVariable(value = "username") String username,

			@RequestParam(
					value = "page",
					required = false,
					defaultValue = AppConstants.DEFAULT_PAGE_NUMBER
			) Integer page,

			@RequestParam(
					value = "size",
					required = false,
					defaultValue = AppConstants.DEFAULT_PAGE_SIZE
			) Integer size) {

		AppUtils.validatePageNumberAndSize(page, size);

		PagedResponse<Post> response =
				postService.getPostsByCreatedBy(
						username,
						page,
						size
				);

		return new ResponseEntity<>(
				response,
				HttpStatus.OK
		);
	}

	@GetMapping("/{username}/albums")
	public ResponseEntity<PagedResponse<Album>> getUserAlbums(
			@PathVariable(name = "username") String username,

			@RequestParam(
					name = "page",
					required = false,
					defaultValue = AppConstants.DEFAULT_PAGE_NUMBER
			) Integer page,

			@RequestParam(
					name = "size",
					required = false,
					defaultValue = AppConstants.DEFAULT_PAGE_SIZE
			) Integer size) {

		AppUtils.validatePageNumberAndSize(page, size);

		PagedResponse<Album> response =
				albumService.getUserAlbums(
						username,
						page,
						size
				);

		return new ResponseEntity<>(
				response,
				HttpStatus.OK
		);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> addUser(
			@Valid @RequestBody User user) {

		User newUser = userService.addUser(user);

		return new ResponseEntity<>(
				newUser,
				HttpStatus.CREATED
		);
	}

	@PutMapping("/{username}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<User> updateUser(
			@Valid @RequestBody User newUser,
			@PathVariable(value = "username") String username,
			@CurrentUser UserPrincipal currentUser) {

		User updatedUser =
				userService.updateUser(
						newUser,
						username,
						currentUser
				);

		return new ResponseEntity<>(
				updatedUser,
				HttpStatus.OK
		);
	}

	@DeleteMapping("/{username}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<ApiResponse> deleteUser(
			@PathVariable(value = "username") String username,
			@CurrentUser UserPrincipal currentUser) {

		ApiResponse apiResponse =
				userService.deleteUser(
						username,
						currentUser
				);

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.OK
		);
	}

	@PutMapping("/{username}/giveAdmin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse> giveAdmin(
			@PathVariable(name = "username") String username) {

		ApiResponse apiResponse =
				userService.giveAdmin(username);

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.OK
		);
	}

	@PutMapping("/{username}/takeAdmin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse> takeAdmin(
			@PathVariable(name = "username") String username) {

		ApiResponse apiResponse =
				userService.removeAdmin(username);

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.OK
		);
	}

	@PutMapping("/setOrUpdateInfo")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<UserProfile> setAddress(
			@CurrentUser UserPrincipal currentUser,
			@Valid @RequestBody InfoRequest infoRequest) {

		UserProfile userProfile =
				userService.setOrUpdateInfo(
						currentUser,
						infoRequest
				);

		return new ResponseEntity<>(
				userProfile,
				HttpStatus.OK
		);
	}
}