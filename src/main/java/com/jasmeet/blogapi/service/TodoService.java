package com.jasmeet.blogapi.service;

import com.jasmeet.blogapi.model.Todo;
import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.PagedResponse;
import com.jasmeet.blogapi.security.UserPrincipal;

public interface TodoService {

	Todo completeTodo(Long id, UserPrincipal currentUser);

	Todo unCompleteTodo(Long id, UserPrincipal currentUser);

	PagedResponse<Todo> getAllTodos(
			UserPrincipal currentUser,
			int page,
			int size
	);

	Todo addTodo(Todo todo, UserPrincipal currentUser);

	Todo getTodo(Long id, UserPrincipal currentUser);

	Todo updateTodo(
			Long id,
			Todo newTodo,
			UserPrincipal currentUser
	);

	ApiResponse deleteTodo(Long id, UserPrincipal currentUser);
}