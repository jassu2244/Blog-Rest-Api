package com.jasmeet.blogapi.service.impl;

import com.jasmeet.blogapi.exception.ResourceNotFoundException;
import com.jasmeet.blogapi.exception.UnauthorizedException;
import com.jasmeet.blogapi.model.Todo;
import com.jasmeet.blogapi.model.user.User;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.repository.TodoRepository;
import com.jasmeet.blogapi.repository.UserRepository;
import com.jasmeet.blogapi.security.UserPrincipal;
import com.jasmeet.blogapi.service.TodoService;
import com.jasmeet.blogapi.utils.AppConstants;
import com.jasmeet.blogapi.utils.AppUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

	private final TodoRepository todoRepository;
	private final UserRepository userRepository;

	public TodoServiceImpl(
			TodoRepository todoRepository,
			UserRepository userRepository) {

		this.todoRepository = todoRepository;
		this.userRepository = userRepository;
	}

	@Override
	public Todo completeTodo(
			Long id,
			UserPrincipal currentUser) {

		Todo todo = todoRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TODO,
								AppConstants.ID,
								id
						)
				);

		User user = userRepository.getUser(currentUser);

		if (todo.getUser() != null
				&& todo.getUser().getId().equals(user.getId())) {

			todo.setCompleted(Boolean.TRUE);

			return todoRepository.save(todo);
		}

		throw new UnauthorizedException(
				new ApiResponse(
						Boolean.FALSE,
						AppConstants.YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION
				)
		);
	}

	@Override
	public Todo unCompleteTodo(
			Long id,
			UserPrincipal currentUser) {

		Todo todo = todoRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TODO,
								AppConstants.ID,
								id
						)
				);

		User user = userRepository.getUser(currentUser);

		if (todo.getUser() != null
				&& todo.getUser().getId().equals(user.getId())) {

			todo.setCompleted(Boolean.FALSE);

			return todoRepository.save(todo);
		}

		throw new UnauthorizedException(
				new ApiResponse(
						Boolean.FALSE,
						AppConstants.YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION
				)
		);
	}

	@Override
	public PagedResponse<Todo> getAllTodos(
			UserPrincipal currentUser,
			int page,
			int size) {

		AppUtils.validatePageNumberAndSize(page, size);

		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.Direction.DESC,
				AppConstants.CREATED_AT
		);

		Page<Todo> todos =
				todoRepository.findByCreatedBy(
						currentUser.getId(),
						pageable
				);

		List<Todo> content = todos.getNumberOfElements() == 0
				? Collections.emptyList()
				: todos.getContent();

		return new PagedResponse<>(
				content,
				todos.getNumber(),
				todos.getSize(),
				todos.getTotalElements(),
				todos.getTotalPages(),
				todos.isLast()
		);
	}

	@Override
	public Todo addTodo(
			Todo todo,
			UserPrincipal currentUser) {

		User user = userRepository.getUser(currentUser);

		todo.setUser(user);

		return todoRepository.save(todo);
	}

	@Override
	public Todo getTodo(
			Long id,
			UserPrincipal currentUser) {

		User user = userRepository.getUser(currentUser);

		Todo todo = todoRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TODO,
								AppConstants.ID,
								id
						)
				);

		if (todo.getUser() != null
				&& todo.getUser().getId().equals(user.getId())) {

			return todo;
		}

		throw new UnauthorizedException(
				new ApiResponse(
						Boolean.FALSE,
						AppConstants.YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION
				)
		);
	}

	@Override
	public Todo updateTodo(
			Long id,
			Todo newTodo,
			UserPrincipal currentUser) {

		User user = userRepository.getUser(currentUser);

		Todo todo = todoRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TODO,
								AppConstants.ID,
								id
						)
				);

		if (todo.getUser() != null
				&& todo.getUser().getId().equals(user.getId())) {

			todo.setTitle(newTodo.getTitle());
			todo.setCompleted(newTodo.getCompleted());

			return todoRepository.save(todo);
		}

		throw new UnauthorizedException(
				new ApiResponse(
						Boolean.FALSE,
						AppConstants.YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION
				)
		);
	}

	@Override
	public ApiResponse deleteTodo(
			Long id,
			UserPrincipal currentUser) {

		User user = userRepository.getUser(currentUser);

		Todo todo = todoRepository
				.findById(id)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								AppConstants.TODO,
								AppConstants.ID,
								id
						)
				);

		if (todo.getUser() != null
				&& todo.getUser().getId().equals(user.getId())) {

			todoRepository.deleteById(id);

			return new ApiResponse(
					Boolean.TRUE,
					"You successfully deleted todo"
			);
		}

		throw new UnauthorizedException(
				new ApiResponse(
						Boolean.FALSE,
						AppConstants.YOU_DON_T_HAVE_PERMISSION_TO_MAKE_THIS_OPERATION
				)
		);
	}
}