package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.exception.ResourceNotFoundException;
import com.jasmeet.blogapi.exception.UnauthorizedException;
import com.jasmeet.blogapi.model.Category;
import com.jasmeet.blogapi.model.role.RoleName;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.repository.CategoryRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.CategoryService;
import com.jasmeet.blogapi.utils.AppConstants;
import com.jasmeet.blogapi.utils.AppUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryServiceImpl(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Override
	public PagedResponse<Category> getAllCategories(int page, int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Category> categories =
				categoryRepository.findAll(pageable);

		List<Category> content =
				categories.getNumberOfElements() == 0
						? Collections.emptyList()
						: categories.getContent();

		return new PagedResponse<>(
				content,
				categories.getNumber(),
				categories.getSize(),
				categories.getTotalElements(),
				categories.getTotalPages(),
				categories.isLast()
		);
	}

	@Override
	public ResponseEntity<Category> getCategory(Long id) {

		Category category = categoryRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.CATEGORY,
								AppConstants.ID,
								id
						)
				);

		return new ResponseEntity<>(
				category,
				HttpStatus.OK
		);
	}

	@Override
	public ResponseEntity<Category> addCategory(
			Category category,
			UserPrincipal currentUser) {

		Category newCategory =
				categoryRepository.save(category);

		return new ResponseEntity<>(
				newCategory,
				HttpStatus.CREATED
		);
	}

	@Override
	public ResponseEntity<Category> updateCategory(
			Long id,
			Category newCategory,
			UserPrincipal currentUser) {

		Category category = categoryRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.CATEGORY,
								AppConstants.ID,
								id
						)
				);

		boolean isOwner = category.getCreatedBy() != null
				&& category.getCreatedBy().equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			category.setName(newCategory.getName());

			Category updatedCategory =
					categoryRepository.save(category);

			return new ResponseEntity<>(
					updatedCategory,
					HttpStatus.OK
			);
		}

		throw new UnauthorizedException(
				"You don't have permission to edit this category"
		);
	}

	@Override
	public ResponseEntity<ApiResponse> deleteCategory(
			Long id,
			UserPrincipal currentUser) {

		Category category = categoryRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.CATEGORY,
								AppConstants.ID,
								id
						)
				);

		boolean isOwner = category.getCreatedBy() != null
				&& category.getCreatedBy().equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			categoryRepository.deleteById(id);

			return new ResponseEntity<>(
					new ApiResponse(
							Boolean.TRUE,
							"You successfully deleted category"
					),
					HttpStatus.OK
			);
		}

		throw new UnauthorizedException(
				"You don't have permission to delete this category"
		);
	}
}