package com.jasmeet.blogapi.controller;

import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.payload.PhotoRequest;
import com.jasmeet.blogapi.payload.PhotoResponse;
import com.jasmeet.blogapi.security.CurrentUser;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.PhotoService;
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
@RequestMapping("/api/photos")
public class PhotoController {

	private final PhotoService photoService;

	public PhotoController(PhotoService photoService) {
		this.photoService = photoService;
	}

	@GetMapping
	public PagedResponse<PhotoResponse> getAllPhotos(
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

		return photoService.getAllPhotos(page, size);
	}

	@PostMapping
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<PhotoResponse> addPhoto(
			@Valid @RequestBody PhotoRequest photoRequest,
			@CurrentUser UserPrincipal currentUser) {

		PhotoResponse photoResponse =
				photoService.addPhoto(photoRequest, currentUser);

		return new ResponseEntity<>(
				photoResponse,
				HttpStatus.CREATED
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PhotoResponse> getPhoto(
			@PathVariable(name = "id") Long id) {

		PhotoResponse photoResponse = photoService.getPhoto(id);

		return new ResponseEntity<>(
				photoResponse,
				HttpStatus.OK
		);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<PhotoResponse> updatePhoto(
			@PathVariable(name = "id") Long id,
			@Valid @RequestBody PhotoRequest photoRequest,
			@CurrentUser UserPrincipal currentUser) {

		PhotoResponse photoResponse =
				photoService.updatePhoto(
						id,
						photoRequest,
						currentUser
				);

		return new ResponseEntity<>(
				photoResponse,
				HttpStatus.OK
		);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<ApiResponse> deletePhoto(
			@PathVariable(name = "id") Long id,
			@CurrentUser UserPrincipal currentUser) {

		ApiResponse apiResponse =
				photoService.deletePhoto(id, currentUser);

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.OK
		);
	}
}