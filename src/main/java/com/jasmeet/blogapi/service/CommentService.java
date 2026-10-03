package com.jasmeet.blogapi.service;

import com.jasmeet.blogapi.model.Comment;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.CommentRequest;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.security.UserPrincipal;

public interface CommentService {

	PagedResponse<Comment> getAllComments(Long postId, int page, int size);

	Comment addComment(
			CommentRequest commentRequest,
			Long postId,
			UserPrincipal currentUser
	);

	Comment getComment(Long postId, Long id);

	Comment updateComment(
			Long postId,
			Long id,
			CommentRequest commentRequest,
			UserPrincipal currentUser
	);

	ApiResponse deleteComment(
			Long postId,
			Long id,
			UserPrincipal currentUser
	);
}