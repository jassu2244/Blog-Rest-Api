package com.jasmeet.blogapi.controller;

import com.jasmeet.blogapi.model.Tag;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.security.CurrentUser;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.TagService;
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
@RequestMapping("/api/tags")
public class TagController {

	private final TagService tagService;

	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	@GetMapping
	public ResponseEntity<PagedResponse<Tag>> getAllTags(
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

		PagedResponse<Tag> response =
				tagService.getAllTags(page, size);

		return new ResponseEntity<>(
				response,
				HttpStatus.OK
		);
	}

	@PostMapping
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<Tag> addTag(
			@Valid @RequestBody Tag tag,
			@CurrentUser UserPrincipal currentUser) {

		Tag newTag = tagService.addTag(tag, currentUser);

		return new ResponseEntity<>(
				newTag,
				HttpStatus.CREATED
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Tag> getTag(
			@PathVariable(name = "id") Long id) {

		Tag tag = tagService.getTag(id);

		return new ResponseEntity<>(
				tag,
				HttpStatus.OK
		);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<Tag> updateTag(
			@PathVariable(name = "id") Long id,
			@Valid @RequestBody Tag tag,
			@CurrentUser UserPrincipal currentUser) {

		Tag updatedTag =
				tagService.updateTag(
						id,
						tag,
						currentUser
				);

		return new ResponseEntity<>(
				updatedTag,
				HttpStatus.OK
		);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<ApiResponse> deleteTag(
			@PathVariable(name = "id") Long id,
			@CurrentUser UserPrincipal currentUser) {

		ApiResponse apiResponse =
				tagService.deleteTag(id, currentUser);

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.OK
		);
	}
}