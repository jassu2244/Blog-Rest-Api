package com.jasmeet.blogapi.service;

import com.jasmeet.blogapi.exception.UnauthorizedException;
import com.jasmeet.blogapi.model.Category;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.security.UserPrincipal;
import org.springframework.http.ResponseEntity;

public interface CategoryService {

	PagedResponse<Category> getAllCategories(int page, int size);

	ResponseEntity<Category> getCategory(Long id);

	ResponseEntity<Category> addCategory(
			Category category,
			UserPrincipal currentUser
	);

	ResponseEntity<Category> updateCategory(
			Long id,
			Category newCategory,
			UserPrincipal currentUser
	) throws UnauthorizedException;

	ResponseEntity<ApiResponse> deleteCategory(
			Long id,
			UserPrincipal currentUser
	) throws UnauthorizedException;
}