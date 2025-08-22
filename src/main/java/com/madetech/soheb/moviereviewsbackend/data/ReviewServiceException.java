package com.madetech.soheb.moviereviewsbackend.data;

/**
 * Exception thrown when review-related operations fail.
 */
public class ReviewServiceException extends MovieReviewServiceException {
    
    public ReviewServiceException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public ReviewServiceException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}