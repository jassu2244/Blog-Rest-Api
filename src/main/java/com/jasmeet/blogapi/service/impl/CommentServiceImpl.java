package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.exception.BlogapiException;
import com.jasmeet.blogapi.exception.ResourceNotFoundException;
import com.jasmeet.blogapi.model.Comment;
import com.jasmeet.blogapi.model.Post;
import com.jasmeet.blogapi.model.role.RoleName;
import com.jasmeet.blogapi.model.user.User;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.CommentRequest;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.repository.CommentRepository;
import com.jasmeet.blogapi.repository.PostRepository;
import com.jasmeet.blogapi.repository.UserRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.CommentService;
import com.jasmeet.blogapi.utils.AppConstants;
import com.jasmeet.blogapi.utils.AppUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

	private static final String THIS_COMMENT = " this comment";

	private static final String YOU_DON_T_HAVE_PERMISSION_TO =
			"You don't have permission to ";

	private static final String COMMENT_DOES_NOT_BELONG_TO_POST =
			"Comment does not belong to post";

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	public CommentServiceImpl(
			CommentRepository commentRepository,
			PostRepository postRepository,
			UserRepository userRepository) {

		this.commentRepository = commentRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
	}

	@Override
	public PagedResponse<Comment> getAllComments(
			Long postId,
			int page,
			int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		// Make sure the post exists before retrieving its comments.
		postRepository.findById(postId)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.POST,
								AppConstants.ID,
								postId
						)
				);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Comment> comments =
				commentRepository.findByPostId(
						postId,
						pageable
				);

		return new PagedResponse<>(
				comments.getContent(),
				comments.getNumber(),
				comments.getSize(),
				comments.getTotalElements(),
				comments.getTotalPages(),
				comments.isLast()
		);
	}

	@Override
	public Comment addComment(
			CommentRequest commentRequest,
			Long postId,
			UserPrincipal currentUser) {

		Post post = postRepository.findById(postId)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.POST,
								AppConstants.ID,
								postId
						)
				);

		User user = userRepository.getUser(currentUser);

		Comment comment = new Comment(
				commentRequest.getBody()
		);

		comment.setUser(user);
		comment.setPost(post);
		comment.setName(currentUser.getUsername());
		comment.setEmail(currentUser.getEmail());

		return commentRepository.save(comment);
	}

	@Override
	public Comment getComment(
			Long postId,
			Long id) {

		Post post = postRepository.findById(postId)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.POST,
								AppConstants.ID,
								postId
						)
				);

		Comment comment = commentRepository.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								"Comment",
								AppConstants.ID,
								id
						)
				);

		if (comment.getPost() != null
				&& comment.getPost().getId().equals(post.getId())) {

			return comment;
		}

		throw new BlogapiException(
				HttpStatus.BAD_REQUEST,
				COMMENT_DOES_NOT_BELONG_TO_POST
		);
	}

	@Override
	public Comment updateComment(
			Long postId,
			Long id,
			CommentRequest commentRequest,
			UserPrincipal currentUser) {

		Post post = postRepository.findById(postId)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.POST,
								AppConstants.ID,
								postId
						)
				);

		Comment comment = commentRepository.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								"Comment",
								AppConstants.ID,
								id
						)
				);

		if (comment.getPost() == null
				|| !comment.getPost().getId().equals(post.getId())) {

			throw new BlogapiException(
					HttpStatus.BAD_REQUEST,
					COMMENT_DOES_NOT_BELONG_TO_POST
			);
		}

		boolean isOwner = comment.getUser() != null
				&& comment.getUser().getId().equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			comment.setBody(commentRequest.getBody());

			return commentRepository.save(comment);
		}

		throw new BlogapiException(
				HttpStatus.UNAUTHORIZED,
				YOU_DON_T_HAVE_PERMISSION_TO
						+ "update"
						+ THIS_COMMENT
		);
	}

	@Override
	public ApiResponse deleteComment(
			Long postId,
			Long id,
			UserPrincipal currentUser) {

		Post post = postRepository.findById(postId)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.POST,
								AppConstants.ID,
								postId
						)
				);

		Comment comment = commentRepository.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								"Comment",
								AppConstants.ID,
								id
						)
				);

		if (comment.getPost() == null
				|| !comment.getPost().getId().equals(post.getId())) {

			return new ApiResponse(
					Boolean.FALSE,
					COMMENT_DOES_NOT_BELONG_TO_POST
			);
		}

		boolean isOwner = comment.getUser() != null
				&& comment.getUser().getId().equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			commentRepository.deleteById(
					comment.getId()
			);

			return new ApiResponse(
					Boolean.TRUE,
					"You successfully deleted comment"
			);
		}

		throw new BlogapiException(
				HttpStatus.UNAUTHORIZED,
				YOU_DON_T_HAVE_PERMISSION_TO
						+ "delete"
						+ THIS_COMMENT
		);
	}
}