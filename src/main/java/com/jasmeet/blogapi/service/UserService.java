package com.jasmeet.blogapi.service;

import com.jasmeet.blogapi.model.user.User;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.InfoRequest;
import com.jasmeet.blogapi.payload.UserIdentityAvailability;
import com.jasmeet.blogapi.payload.UserProfile;
import com.jasmeet.blogapi.payload.UserSummary;
import com.jasmeet.blogapi.security.UserPrincipal;

public interface UserService {

	UserSummary getCurrentUser(UserPrincipal currentUser);

	UserIdentityAvailability checkUsernameAvailability(String username);

	UserIdentityAvailability checkEmailAvailability(String email);

	UserProfile getUserProfile(String username);

	User addUser(User user);

	User updateUser(User newUser, String username, UserPrincipal currentUser);

	ApiResponse deleteUser(String username, UserPrincipal currentUser);

	ApiResponse giveAdmin(String username);

	ApiResponse removeAdmin(String username);

	UserProfile setOrUpdateInfo(
			UserPrincipal currentUser,
			InfoRequest infoRequest
	);
}