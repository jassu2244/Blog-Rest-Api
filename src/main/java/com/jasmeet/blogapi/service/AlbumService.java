package com.jasmeet.blogapi.service;

import com.jasmeet.blogapi.model.Album;
import com.jasmeet.blogapi.payload.AlbumResponse;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.payload.request.AlbumRequest;
import com.jasmeet.blogapi.security.UserPrincipal;
import org.springframework.http.ResponseEntity;

public interface AlbumService {

	PagedResponse<AlbumResponse> getAllAlbums(int page, int size);

	ResponseEntity<Album> addAlbum(
			AlbumRequest albumRequest,
			UserPrincipal currentUser
	);

	ResponseEntity<Album> getAlbum(Long id);

	ResponseEntity<AlbumResponse> updateAlbum(
			Long id,
			AlbumRequest newAlbum,
			UserPrincipal currentUser
	);

	ResponseEntity<ApiResponse> deleteAlbum(
			Long id,
			UserPrincipal currentUser
	);

	PagedResponse<Album> getUserAlbums(
			String username,
			int page,
			int size
	);
}