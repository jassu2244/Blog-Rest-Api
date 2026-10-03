package com.jasmeet.blogapi.exception;

import com.jasmeet.blogapi.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private transient ApiResponse apiResponse;

	private final String resourceName;
	private final String fieldName;
	private final Object fieldValue;

	public ResourceNotFoundException(
			String resourceName,
			String fieldName,
			Object fieldValue) {

		super(String.format(
				"%s not found with %s: '%s'",
				resourceName,
				fieldName,
				fieldValue
		));

		this.resourceName = resourceName;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;

		setApiResponse();
	}

	public String getResourceName() {
		return resourceName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Object getFieldValue() {
		return fieldValue;
	}

	public ApiResponse getApiResponse() {
		return apiResponse;
	}

	private void setApiResponse() {
		String message = String.format(
				"%s not found with %s: '%s'",
				resourceName,
				fieldName,
				fieldValue
		);

		this.apiResponse = new ApiResponse(Boolean.FALSE, message);
	}
}