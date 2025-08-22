package com.madetech.soheb.moviereviewsbackend.data.exceptions;

/**
 * Exception thrown when movie-related operations fail.
 */
public class MovieServiceException extends MovieReviewServiceException {
    
    public MovieServiceException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public MovieServiceException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}