package com.jasmeet.blogapi.utils;

import com.jasmeet.blogapi.exception.BlogapiException;
import org.springframework.http.HttpStatus;

public final class AppUtils {

	private AppUtils() {
		// Utility class
	}

	public static void validatePageNumberAndSize(int page, int size) {

		if (page < 0) {
			throw new BlogapiException(
					HttpStatus.BAD_REQUEST,
					"Page number cannot be less than zero."
			);
		}

		if (size < 0) {
			throw new BlogapiException(
					HttpStatus.BAD_REQUEST,
					"Size number cannot be less than zero."
			);
		}

		if (size > AppConstants.MAX_PAGE_SIZE) {
			throw new BlogapiException(
					HttpStatus.BAD_REQUEST,
					"Page size must not be greater than "
							+ AppConstants.MAX_PAGE_SIZE
			);
		}
	}
}