package com.madetech.soheb.moviereviewsbackend.data;

/**
 * Exception thrown when authentication-related operations fail.
 */
public class AuthenticationException extends MovieReviewServiceException {
    
    public AuthenticationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public AuthenticationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}