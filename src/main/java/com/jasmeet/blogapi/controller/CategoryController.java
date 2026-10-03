package com.jasmeet.blogapi.controller;

import com.jasmeet.blogapi.exception.UnauthorizedException;
import com.jasmeet.blogapi.model.Category;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.security.CurrentUser;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.CategoryService;
import com.jasmeet.blogapi.utils.AppConstants;
import com.jasmeet.blogapi.utils.AppUtils;
import jakarta.validation.Valid;
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
@RequestMapping("/api/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public PagedResponse<Category> getAllCategories(
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

		return categoryService.getAllCategories(page, size);
	}

	@PostMapping
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Category> addCategory(
			@Valid @RequestBody Category category,
			@CurrentUser UserPrincipal currentUser) {

		return categoryService.addCategory(category, currentUser);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Category> getCategory(
			@PathVariable(name = "id") Long id) {

		return categoryService.getCategory(id);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<Category> updateCategory(
			@PathVariable(name = "id") Long id,
			@Valid @RequestBody Category category,
			@CurrentUser UserPrincipal currentUser)
			throws UnauthorizedException {

		return categoryService.updateCategory(
				id,
				category,
				currentUser
		);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<ApiResponse> deleteCategory(
			@PathVariable(name = "id") Long id,
			@CurrentUser UserPrincipal currentUser)
			throws UnauthorizedException {

		return categoryService.deleteCategory(
				id,
				currentUser
		);
	}
}