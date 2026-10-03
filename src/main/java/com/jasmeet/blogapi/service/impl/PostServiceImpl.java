package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.exception.ResourceNotFoundException;
import com.jasmeet.blogapi.exception.UnauthorizedException;
import com.jasmeet.blogapi.model.Category;
import com.jasmeet.blogapi.model.Post;
import com.jasmeet.blogapi.model.Tag;
import com.jasmeet.blogapi.model.role.RoleName;
import com.jasmeet.blogapi.model.user.User;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.payload.PostRequest;
import com.jasmeet.blogapi.payload.PostResponse;
import com.jasmeet.blogapi.repository.CategoryRepository;
import com.jasmeet.blogapi.repository.PostRepository;
import com.jasmeet.blogapi.repository.TagRepository;
import com.jasmeet.blogapi.repository.UserRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.PostService;
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
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final TagRepository tagRepository;

	public PostServiceImpl(
			PostRepository postRepository,
			UserRepository userRepository,
			CategoryRepository categoryRepository,
			TagRepository tagRepository) {

		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.categoryRepository = categoryRepository;
		this.tagRepository = tagRepository;
	}

	@Override
	public PagedResponse<Post> getAllPosts(int page, int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Post> posts = postRepository.findAll(pageable);

		List<Post> content = posts.getNumberOfElements() == 0
				? Collections.emptyList()
				: posts.getContent();

		return new PagedResponse<>(
				content,
				posts.getNumber(),
				posts.getSize(),
				posts.getTotalElements(),
				posts.getTotalPages(),
				posts.isLast()
		);
	}

	@Override
	public PagedResponse<Post> getPostsByCreatedBy(
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

		Page<Post> posts =
				postRepository.findByCreatedBy(
						user.getId(),
						pageable
				);

		List<Post> content = posts.getNumberOfElements() == 0
				? Collections.emptyList()
				: posts.getContent();

		return new PagedResponse<>(
				content,
				posts.getNumber(),
				posts.getSize(),
				posts.getTotalElements(),
				posts.getTotalPages(),
				posts.isLast()
		);
	}

	@Override
	public PagedResponse<Post> getPostsByCategory(
			Long id,
			int page,
			int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		Category category = categoryRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.CATEGORY,
								AppConstants.ID,
								id
						)
				);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Post> posts =
				postRepository.findByCategory(
						category.getId(),
						pageable
				);

		List<Post> content = posts.getNumberOfElements() == 0
				? Collections.emptyList()
				: posts.getContent();

		return new PagedResponse<>(
				content,
				posts.getNumber(),
				posts.getSize(),
				posts.getTotalElements(),
				posts.getTotalPages(),
				posts.isLast()
		);
	}

	@Override
	public PagedResponse<Post> getPostsByTag(
			Long id,
			int page,
			int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		Tag tag = tagRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TAG,
								AppConstants.ID,
								id
						)
				);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Post> posts =
				postRepository.findByTags(
						Collections.singletonList(tag),
						pageable
				);

		List<Post> content = posts.getNumberOfElements() == 0
				? Collections.emptyList()
				: posts.getContent();

		return new PagedResponse<>(
				content,
				posts.getNumber(),
				posts.getSize(),
				posts.getTotalElements(),
				posts.getTotalPages(),
				posts.isLast()
		);
	}

	@Override
	public Post updatePost(
			Long id,
			PostRequest newPostRequest,
			UserPrincipal currentUser) {

		Post post = postRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.POST,
								AppConstants.ID,
								id
						)
				);

		Category category = categoryRepository
				.findById(newPostRequest.getCategoryId())
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.CATEGORY,
								AppConstants.ID,
								newPostRequest.getCategoryId()
						)
				);

		boolean isOwner = post.getUser() != null
				&& post.getUser()
				.getId()
				.equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			post.setTitle(newPostRequest.getTitle());
			post.setBody(newPostRequest.getBody());
			post.setCategory(category);

			/*
			 * Update tags as well when they are supplied in the request.
			 */
			List<Tag> tags = new ArrayList<>();

			for (String name : newPostRequest.getTags()) {
				if (name == null || name.isBlank()) {
					continue;
				}

				Tag tag = tagRepository.findByName(name);

				if (tag == null) {
					tag = tagRepository.save(
							new Tag(name)
					);
				}

				tags.add(tag);
			}

			post.setTags(tags);

			return postRepository.save(post);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to edit this post"
		);

		throw new UnauthorizedException(apiResponse);
	}

	@Override
	public ApiResponse deletePost(
			Long id,
			UserPrincipal currentUser) {

		Post post = postRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.POST,
								AppConstants.ID,
								id
						)
				);

		boolean isOwner = post.getUser() != null
				&& post.getUser()
				.getId()
				.equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			postRepository.deleteById(id);

			return new ApiResponse(
					Boolean.TRUE,
					"You successfully deleted post"
			);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to delete this post"
		);

		throw new UnauthorizedException(apiResponse);
	}

	@Override
	public PostResponse addPost(
			PostRequest postRequest,
			UserPrincipal currentUser) {

		User user = userRepository
				.findById(currentUser.getId())
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.USER,
								AppConstants.ID,
								currentUser.getId()
						)
				);

		Category category = categoryRepository
				.findById(postRequest.getCategoryId())
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.CATEGORY,
								AppConstants.ID,
								postRequest.getCategoryId()
						)
				);

		List<Tag> tags = new ArrayList<>(
				postRequest.getTags().size()
		);

		for (String name : postRequest.getTags()) {

			if (name == null || name.isBlank()) {
				continue;
			}

			Tag tag = tagRepository.findByName(name);

			if (tag == null) {
				tag = tagRepository.save(
						new Tag(name)
				);
			}

			tags.add(tag);
		}

		Post post = new Post();

		post.setBody(postRequest.getBody());
		post.setTitle(postRequest.getTitle());
		post.setCategory(category);
		post.setUser(user);
		post.setTags(tags);

		Post newPost = postRepository.save(post);

		return convertToPostResponse(newPost);
	}

	@Override
	public Post getPost(Long id) {

		return postRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.POST,
								AppConstants.ID,
								id
						)
				);
	}

	private PostResponse convertToPostResponse(Post post) {

		PostResponse postResponse = new PostResponse();

		postResponse.setTitle(post.getTitle());
		postResponse.setBody(post.getBody());

		if (post.getCategory() != null) {
			postResponse.setCategory(
					post.getCategory().getName()
			);
		}

		List<String> tagNames = new ArrayList<>();

		if (post.getTags() != null) {
			for (Tag tag : post.getTags()) {
				if (tag != null) {
					tagNames.add(tag.getName());
				}
			}
		}

		postResponse.setTags(tagNames);

		return postResponse;
	}
}