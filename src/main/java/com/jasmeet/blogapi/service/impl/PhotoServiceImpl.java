package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.exception.ResourceNotFoundException;
import com.jasmeet.blogapi.exception.UnauthorizedException;
import com.jasmeet.blogapi.model.Album;
import com.jasmeet.blogapi.model.Photo;
import com.jasmeet.blogapi.model.role.RoleName;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.payload.PhotoRequest;
import com.jasmeet.blogapi.payload.PhotoResponse;
import com.jasmeet.blogapi.repository.AlbumRepository;
import com.jasmeet.blogapi.repository.PhotoRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.PhotoService;
import com.jasmeet.blogapi.utils.AppConstants;
import com.jasmeet.blogapi.utils.AppUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PhotoServiceImpl implements PhotoService {

	private final PhotoRepository photoRepository;
	private final AlbumRepository albumRepository;

	public PhotoServiceImpl(
			PhotoRepository photoRepository,
			AlbumRepository albumRepository) {

		this.photoRepository = photoRepository;
		this.albumRepository = albumRepository;
	}

	@Override
	public PagedResponse<PhotoResponse> getAllPhotos(
			int page,
			int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Photo> photos =
				photoRepository.findAll(pageable);

		if (photos.getNumberOfElements() == 0) {
			return new PagedResponse<>(
					Collections.emptyList(),
					photos.getNumber(),
					photos.getSize(),
					photos.getTotalElements(),
					photos.getTotalPages(),
					photos.isLast()
			);
		}

		List<PhotoResponse> photoResponses =
				new ArrayList<>(photos.getContent().size());

		for (Photo photo : photos.getContent()) {
			photoResponses.add(toPhotoResponse(photo));
		}

		return new PagedResponse<>(
				photoResponses,
				photos.getNumber(),
				photos.getSize(),
				photos.getTotalElements(),
				photos.getTotalPages(),
				photos.isLast()
		);
	}

	@Override
	public PhotoResponse getPhoto(Long id) {

		Photo photo = photoRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.PHOTO,
								AppConstants.ID,
								id
						)
				);

		return toPhotoResponse(photo);
	}

	@Override
	public PhotoResponse updatePhoto(
			Long id,
			PhotoRequest photoRequest,
			UserPrincipal currentUser) {

		Photo photo = photoRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.PHOTO,
								AppConstants.ID,
								id
						)
				);

		Album newAlbum = albumRepository
				.findById(photoRequest.getAlbumId())
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.ALBUM,
								AppConstants.ID,
								photoRequest.getAlbumId()
						)
				);

		boolean isOwner = photo.getAlbum() != null
				&& photo.getAlbum().getUser() != null
				&& photo.getAlbum()
				.getUser()
				.getId()
				.equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			photo.setTitle(photoRequest.getTitle());
			photo.setUrl(photoRequest.getUrl());
			photo.setThumbnailUrl(photoRequest.getThumbnailUrl());
			photo.setAlbum(newAlbum);

			Photo updatedPhoto =
					photoRepository.save(photo);

			return toPhotoResponse(updatedPhoto);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to update this photo"
		);

		throw new UnauthorizedException(apiResponse);
	}

	@Override
	public PhotoResponse addPhoto(
			PhotoRequest photoRequest,
			UserPrincipal currentUser) {

		Album album = albumRepository
				.findById(photoRequest.getAlbumId())
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.ALBUM,
								AppConstants.ID,
								photoRequest.getAlbumId()
						)
				);

		boolean isOwner = album.getUser() != null
				&& album.getUser()
				.getId()
				.equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			Photo photo = new Photo(
					photoRequest.getTitle(),
					photoRequest.getUrl(),
					photoRequest.getThumbnailUrl(),
					album
			);

			Photo newPhoto =
					photoRepository.save(photo);

			return toPhotoResponse(newPhoto);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to add photo in this album"
		);

		throw new UnauthorizedException(apiResponse);
	}

	@Override
	public ApiResponse deletePhoto(
			Long id,
			UserPrincipal currentUser) {

		Photo photo = photoRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.PHOTO,
								AppConstants.ID,
								id
						)
				);

		boolean isOwner = photo.getAlbum() != null
				&& photo.getAlbum().getUser() != null
				&& photo.getAlbum()
				.getUser()
				.getId()
				.equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			photoRepository.deleteById(id);

			return new ApiResponse(
					Boolean.TRUE,
					"Photo deleted successfully"
			);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to delete this photo"
		);

		throw new UnauthorizedException(apiResponse);
	}

	@Override
	public PagedResponse<PhotoResponse> getAllPhotosByAlbum(
			Long albumId,
			int page,
			int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		// Make sure the album exists.
		albumRepository
				.findById(albumId)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.ALBUM,
								AppConstants.ID,
								albumId
						)
				);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Photo> photos =
				photoRepository.findByAlbumId(
						albumId,
						pageable
				);

		List<PhotoResponse> photoResponses =
				new ArrayList<>(photos.getContent().size());

		for (Photo photo : photos.getContent()) {
			photoResponses.add(toPhotoResponse(photo));
		}

		return new PagedResponse<>(
				photoResponses,
				photos.getNumber(),
				photos.getSize(),
				photos.getTotalElements(),
				photos.getTotalPages(),
				photos.isLast()
		);
	}

	private PhotoResponse toPhotoResponse(Photo photo) {

		Long albumId = photo.getAlbum() != null
				? photo.getAlbum().getId()
				: null;

		return new PhotoResponse(
				photo.getId(),
				photo.getTitle(),
				photo.getUrl(),
				photo.getThumbnailUrl(),
				albumId
		);
	}
}