package com.jasmeet.blogapi.exception;

import org.springframework.http.HttpStatus;

public class BlogapiException extends RuntimeException {

	private static final long serialVersionUID = -6593330219878485669L;

	private final HttpStatus status;
	private final String message;

	public BlogapiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
		this.message = message;
	}

	public BlogapiException(
			HttpStatus status,
			String message,
			Throwable exception) {

		super(message, exception);
		this.status = status;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getMessage() {
		return message;
	}
}