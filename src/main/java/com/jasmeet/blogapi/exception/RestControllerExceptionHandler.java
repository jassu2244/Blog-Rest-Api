package com.jasmeet.blogapi.exception;

import com.jasmeet.blogapi.payload.ApiResponse;
import com.jasmeet.blogapi.payload.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
public class RestControllerExceptionHandler {

	@ExceptionHandler(BlogapiException.class)
	@ResponseBody
	public ResponseEntity<ApiResponse> resolveException(BlogapiException exception) {

		String message = exception.getMessage();
		HttpStatus status = exception.getStatus();

		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setSuccess(Boolean.FALSE);
		apiResponse.setMessage(message);

		return new ResponseEntity<>(apiResponse, status);
	}

	@ExceptionHandler(UnauthorizedException.class)
	@ResponseBody
	public ResponseEntity<ApiResponse> resolveException(
			UnauthorizedException exception) {

		ApiResponse apiResponse = exception.getApiResponse();

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.UNAUTHORIZED
		);
	}

	@ExceptionHandler(BadRequestException.class)
	@ResponseBody
	public ResponseEntity<ApiResponse> resolveException(
			BadRequestException exception) {

		ApiResponse apiResponse = exception.getApiResponse();

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.BAD_REQUEST
		);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseBody
	public ResponseEntity<ApiResponse> resolveException(
			ResourceNotFoundException exception) {

		ApiResponse apiResponse = exception.getApiResponse();

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.NOT_FOUND
		);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseBody
	public ResponseEntity<ApiResponse> resolveException(
			AccessDeniedException exception) {

		ApiResponse apiResponse = exception.getApiResponse();

		return new ResponseEntity<>(
				apiResponse,
				HttpStatus.FORBIDDEN
		);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	public ResponseEntity<ExceptionResponse> resolveException(
			MethodArgumentNotValidException ex) {

		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

		List<String> messages = new ArrayList<>(fieldErrors.size());

		for (FieldError error : fieldErrors) {
			messages.add(
					error.getField() + " - " + error.getDefaultMessage()
			);
		}

		ExceptionResponse exceptionResponse = new ExceptionResponse(
				messages,
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				HttpStatus.BAD_REQUEST.value()
		);

		return new ResponseEntity<>(
				exceptionResponse,
				HttpStatus.BAD_REQUEST
		);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseBody
	public ResponseEntity<ExceptionResponse> resolveException(
			MethodArgumentTypeMismatchException ex) {

		String message = "Parameter '"
				+ ex.getParameter().getParameterName()
				+ "' must be '"
				+ Objects.requireNonNull(ex.getRequiredType()).getSimpleName()
				+ "'";

		List<String> messages = new ArrayList<>(1);
		messages.add(message);

		ExceptionResponse exceptionResponse = new ExceptionResponse(
				messages,
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				HttpStatus.BAD_REQUEST.value()
		);

		return new ResponseEntity<>(
				exceptionResponse,
				HttpStatus.BAD_REQUEST
		);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseBody
	public ResponseEntity<ExceptionResponse> resolveException(
			HttpRequestMethodNotSupportedException ex) {

		String message = "Request method '"
				+ ex.getMethod()
				+ "' not supported. List of all supported methods - "
				+ ex.getSupportedHttpMethods();

		List<String> messages = new ArrayList<>(1);
		messages.add(message);

		ExceptionResponse exceptionResponse = new ExceptionResponse(
				messages,
				HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
				HttpStatus.METHOD_NOT_ALLOWED.value()
		);

		return new ResponseEntity<>(
				exceptionResponse,
				HttpStatus.METHOD_NOT_ALLOWED
		);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseBody
	public ResponseEntity<ExceptionResponse> resolveException(
			HttpMessageNotReadableException ex) {

		String message =
				"Please provide Request Body in valid JSON format";

		List<String> messages = new ArrayList<>(1);
		messages.add(message);

		ExceptionResponse exceptionResponse = new ExceptionResponse(
				messages,
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				HttpStatus.BAD_REQUEST.value()
		);

		return new ResponseEntity<>(
				exceptionResponse,
				HttpStatus.BAD_REQUEST
		);
	}
}