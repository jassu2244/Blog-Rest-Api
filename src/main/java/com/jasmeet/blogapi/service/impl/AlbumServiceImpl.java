package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.exception.BlogapiException;
import com.jasmeet.blogapi.exception.ResourceNotFoundException;
import com.jasmeet.blogapi.model.Album;
import com.jasmeet.blogapi.model.role.RoleName;
import com.jasmeet.blogapi.model.user.User;
import com.jasmeet.blogapi.payload.AlbumResponse;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.payload.request.AlbumRequest;
import com.jasmeet.blogapi.repository.AlbumRepository;
import com.jasmeet.blogapi.repository.UserRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.AlbumService;
import com.jasmeet.blogapi.utils.AppConstants;
import com.jasmeet.blogapi.utils.AppUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class AlbumServiceImpl implements AlbumService {

	private static final String ALBUM_STR = AppConstants.ALBUM;

	private static final String
			YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION =
			AppConstants.YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION;

	private final AlbumRepository albumRepository;
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;

	public AlbumServiceImpl(
			AlbumRepository albumRepository,
			UserRepository userRepository,
			ModelMapper modelMapper) {

		this.albumRepository = albumRepository;
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	public PagedResponse<AlbumResponse> getAllAlbums(int page, int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Album> albums = albumRepository.findAll(pageable);

		if (albums.getNumberOfElements() == 0) {
			return new PagedResponse<>(
					Collections.emptyList(),
					albums.getNumber(),
					albums.getSize(),
					albums.getTotalElements(),
					albums.getTotalPages(),
					albums.isLast()
			);
		}

		List<AlbumResponse> albumResponses =
				Arrays.asList(
						modelMapper.map(
								albums.getContent(),
								AlbumResponse[].class
						)
				);

		return new PagedResponse<>(
				albumResponses,
				albums.getNumber(),
				albums.getSize(),
				albums.getTotalElements(),
				albums.getTotalPages(),
				albums.isLast()
		);
	}

	@Override
	public ResponseEntity<Album> addAlbum(
			AlbumRequest albumRequest,
			UserPrincipal currentUser) {

		User user = userRepository.getUser(currentUser);

		Album album = new Album();

		modelMapper.map(albumRequest, album);

		album.setUser(user);

		Album newAlbum = albumRepository.save(album);

		return new ResponseEntity<>(
				newAlbum,
				HttpStatus.CREATED
		);
	}

	@Override
	public ResponseEntity<Album> getAlbum(Long id) {

		Album album = albumRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								ALBUM_STR,
								AppConstants.ID,
								id
						)
				);

		return new ResponseEntity<>(
				album,
				HttpStatus.OK
		);
	}

	@Override
	public ResponseEntity<AlbumResponse> updateAlbum(
			Long id,
			AlbumRequest newAlbum,
			UserPrincipal currentUser) {

		Album album = albumRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								ALBUM_STR,
								AppConstants.ID,
								id
						)
				);

		User user = userRepository.getUser(currentUser);

		boolean isOwner = album.getUser() != null
				&& album.getUser().getId().equals(user.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			album.setTitle(newAlbum.getTitle());

			Album updatedAlbum = albumRepository.save(album);

			AlbumResponse albumResponse = new AlbumResponse();

			modelMapper.map(
					updatedAlbum,
					albumResponse
			);

			return new ResponseEntity<>(
					albumResponse,
					HttpStatus.OK
			);
		}

		throw new BlogapiException(
				HttpStatus.UNAUTHORIZED,
				YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION
		);
	}

	@Override
	public ResponseEntity<ApiResponse> deleteAlbum(
			Long id,
			UserPrincipal currentUser) {

		Album album = albumRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								ALBUM_STR,
								AppConstants.ID,
								id
						)
				);

		User user = userRepository.getUser(currentUser);

		boolean isOwner = album.getUser() != null
				&& album.getUser().getId().equals(user.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			albumRepository.deleteById(id);

			return new ResponseEntity<>(
					new ApiResponse(
							Boolean.TRUE,
							"You successfully deleted album"
					),
					HttpStatus.OK
			);
		}

		throw new BlogapiException(
				HttpStatus.UNAUTHORIZED,
				YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION
		);
	}

	@Override
	public PagedResponse<Album> getUserAlbums(
			String username,
			int page,
			int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		User user = userRepository.getUserByName(username);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Album> albums =
				albumRepository.findByCreatedBy(
						user.getId(),
						pageable
				);

		List<Album> content =
				albums.getNumberOfElements() > 0
						? albums.getContent()
						: Collections.emptyList();

		return new PagedResponse<>(
				content,
				albums.getNumber(),
				albums.getSize(),
				albums.getTotalElements(),
				albums.getTotalPages(),
				albums.isLast()
		);
	}
}