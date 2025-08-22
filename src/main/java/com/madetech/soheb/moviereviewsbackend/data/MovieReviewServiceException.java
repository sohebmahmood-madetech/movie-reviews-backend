package com.madetech.soheb.moviereviewsbackend.data;

/**
 * Base exception class for all service layer exceptions in the Movie Review application.
 * Provides a standardized way to handle errors with specific error codes for debugging.
 */
public class MovieReviewServiceException extends RuntimeException {
    
    private final String errorCode;
    
    public MovieReviewServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public MovieReviewServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}