package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.exception.ResourceNotFoundException;
import com.jasmeet.blogapi.exception.UnauthorizedException;
import com.jasmeet.blogapi.model.Tag;
import com.jasmeet.blogapi.model.role.RoleName;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.repository.TagRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.TagService;
import com.jasmeet.blogapi.utils.AppConstants;
import com.jasmeet.blogapi.utils.AppUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

	private final TagRepository tagRepository;

	public TagServiceImpl(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}

	@Override
	public PagedResponse<Tag> getAllTags(int page, int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Tag> tags = tagRepository.findAll(pageable);

		List<Tag> content = tags.getNumberOfElements() == 0
				? Collections.emptyList()
				: tags.getContent();

		return new PagedResponse<>(
				content,
				tags.getNumber(),
				tags.getSize(),
				tags.getTotalElements(),
				tags.getTotalPages(),
				tags.isLast()
		);
	}

	@Override
	public Tag getTag(Long id) {

		return tagRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TAG,
								AppConstants.ID,
								id
						)
				);
	}

	@Override
	public Tag addTag(
			Tag tag,
			UserPrincipal currentUser) {

		return tagRepository.save(tag);
	}

	@Override
	public Tag updateTag(
			Long id,
			Tag newTag,
			UserPrincipal currentUser) {

		Tag tag = tagRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TAG,
								AppConstants.ID,
								id
						)
				);

		boolean isOwner = tag.getCreatedBy() != null
				&& tag.getCreatedBy().equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			tag.setName(newTag.getName());

			return tagRepository.save(tag);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to edit this tag"
		);

		throw new UnauthorizedException(apiResponse);
	}

	@Override
	public ApiResponse deleteTag(
			Long id,
			UserPrincipal currentUser) {

		Tag tag = tagRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TAG,
								AppConstants.ID,
								id
						)
				);

		boolean isOwner = tag.getCreatedBy() != null
				&& tag.getCreatedBy().equals(currentUser.getId());

		boolean isAdmin = currentUser.getAuthorities()
				.contains(
						new SimpleGrantedAuthority(
								RoleName.ROLE_ADMIN.toString()
						)
				);

		if (isOwner || isAdmin) {

			tagRepository.deleteById(id);

			return new ApiResponse(
					Boolean.TRUE,
					"You successfully deleted tag"
			);
		}

		ApiResponse apiResponse = new ApiResponse(
				Boolean.FALSE,
				"You don't have permission to delete this tag"
		);

		throw new UnauthorizedException(apiResponse);
	}
}