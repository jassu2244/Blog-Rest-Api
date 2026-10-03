package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.model.user.User;
import com.jasmeet.blogapi.repository.UserRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsServiceImpl
		implements UserDetailsService, CustomUserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String usernameOrEmail) {

		User user = userRepository
				.findByUsernameOrEmail(
						usernameOrEmail,
						usernameOrEmail
				)
				.orElseThrow(
						() -> new UsernameNotFoundException(
								String.format(
										"User not found with this username or email: %s",
										usernameOrEmail
								)
						)
				);

		return UserPrincipal.create(user);
	}

	@Override
	@Transactional
	public UserDetails loadUserById(Long id) {

		User user = userRepository
				.findById(id)
				.orElseThrow(
						() -> new UsernameNotFoundException(
								String.format(
										"User not found with id: %s",
										id
								)
						)
				);

		return UserPrincipal.create(user);
	}
}