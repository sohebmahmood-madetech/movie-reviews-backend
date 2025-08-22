package com.madetech.soheb.moviereviewsbackend.config;

import com.madetech.soheb.moviereviewsbackend.data.controller.ApiError;
import com.madetech.soheb.moviereviewsbackend.data.controller.ApiResponse;
import com.madetech.soheb.moviereviewsbackend.data.exceptions.AuthenticationException;
import com.madetech.soheb.moviereviewsbackend.data.exceptions.MovieReviewServiceException;
import com.madetech.soheb.moviereviewsbackend.data.exceptions.MovieServiceException;
import com.madetech.soheb.moviereviewsbackend.data.exceptions.ReviewServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Security error codes - used for tracking in logs but don't reveal internal structure
    private static final long ERR_AUTHENTICATION_FAILED = 1001L;
    private static final long ERR_ACCESS_DENIED = 1002L;
    private static final long ERR_VALIDATION_FAILED = 2001L;
    private static final long ERR_INVALID_REQUEST_FORMAT = 2002L;
    private static final long ERR_MISSING_PARAMETER = 2003L;
    private static final long ERR_INVALID_PARAMETER_TYPE = 2004L;
    private static final long ERR_METHOD_NOT_SUPPORTED = 2005L;
    private static final long ERR_RESOURCE_NOT_FOUND = 2006L;
    private static final long ERR_DATA_INTEGRITY_VIOLATION = 3001L;
    private static final long ERR_BUSINESS_LOGIC = 4001L;
    private static final long ERR_INTERNAL_SERVER = 5001L;
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            com.madetech.soheb.moviereviewsbackend.data.exceptions.AuthenticationException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Authentication error - {}", ERR_AUTHENTICATION_FAILED, ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(createErrorResponse(ERR_AUTHENTICATION_FAILED, "Authentication failed"));
    }
    
    @ExceptionHandler({org.springframework.security.core.AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Object>> handleSpringAuthenticationException(
            org.springframework.security.core.AuthenticationException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Spring security authentication error - {}", ERR_AUTHENTICATION_FAILED, ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(createErrorResponse(ERR_AUTHENTICATION_FAILED, "Authentication failed"));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Access denied for {} - {}", ERR_ACCESS_DENIED, request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(createErrorResponse(ERR_ACCESS_DENIED, "Access denied"));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Validation failed for {} - {}", ERR_VALIDATION_FAILED, request.getRequestURI(), ex.getMessage(), ex);
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        StringBuilder message = new StringBuilder("Validation failed");
        if (!errors.isEmpty()) {
            message.append(": ");
            message.append(String.join(", ", errors.values()));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(ERR_VALIDATION_FAILED, message.toString()));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Constraint validation failed for {} - {}", ERR_VALIDATION_FAILED, request.getRequestURI(), ex.getMessage(), ex);
        
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        StringBuilder message = new StringBuilder("Validation failed");
        
        if (!violations.isEmpty()) {
            message.append(": ");
            message.append(violations.stream()
                .map(ConstraintViolation::getMessage)
                .reduce((a, b) -> a + ", " + b)
                .orElse(""));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(ERR_VALIDATION_FAILED, message.toString()));
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Invalid request format for {} - {}", ERR_INVALID_REQUEST_FORMAT, request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(ERR_INVALID_REQUEST_FORMAT, "Invalid request format"));
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Missing parameter for {} - {}", ERR_MISSING_PARAMETER, request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(ERR_MISSING_PARAMETER, "Missing required parameter: " + ex.getParameterName()));
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Parameter type mismatch for {} - {}", ERR_INVALID_PARAMETER_TYPE, request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(ERR_INVALID_PARAMETER_TYPE, "Invalid parameter type for: " + ex.getName()));
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Method not supported for {} - {}", ERR_METHOD_NOT_SUPPORTED, request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(createErrorResponse(ERR_METHOD_NOT_SUPPORTED, "HTTP method not supported"));
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Resource not found for {} - {}", ERR_RESOURCE_NOT_FOUND, request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(createErrorResponse(ERR_RESOURCE_NOT_FOUND, "Resource not found"));
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Data integrity violation for {} - {}", ERR_DATA_INTEGRITY_VIOLATION, request.getRequestURI(), ex.getMessage(), ex);
        
        // Check for common database constraint violations
        String message = "Data integrity violation";
        String errorMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        
        if (errorMessage.contains("unique") || errorMessage.contains("duplicate")) {
            message = "Record already exists";
        } else if (errorMessage.contains("foreign key") || errorMessage.contains("constraint")) {
            message = "Invalid reference data";
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(ERR_DATA_INTEGRITY_VIOLATION, message));
    }
    
    @ExceptionHandler({MovieServiceException.class, ReviewServiceException.class, MovieReviewServiceException.class})
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            Exception ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Business logic error for {} - {}", ERR_BUSINESS_LOGIC, request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(createErrorResponse(ERR_BUSINESS_LOGIC, "Request could not be processed"));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        logger.error("ERR_{}: Unexpected error for {} - {}", ERR_INTERNAL_SERVER, request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(createErrorResponse(ERR_INTERNAL_SERVER, "An unexpected error occurred"));
    }
    
    private ApiResponse<Object> createErrorResponse(long errorCode, String message) {
        ApiError error = new ApiError(errorCode, message);
        return ApiResponse.failure(error);
    }
}