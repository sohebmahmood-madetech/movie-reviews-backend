package com.madetech.soheb.moviereviewsbackend.data.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    
    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;
    
    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true; // Let @NotNull handle null values
        }
        
        StringBuilder errorMessage = new StringBuilder();
        boolean isValid = true;
        
        // Check minimum length
        if (password.length() < minLength) {
            errorMessage.append("Password must be at least ").append(minLength).append(" characters long. ");
            isValid = false;
        }
        
        // Check for uppercase letter
        if (requireUppercase && !password.chars().anyMatch(Character::isUpperCase)) {
            errorMessage.append("Password must contain at least one uppercase letter. ");
            isValid = false;
        }
        
        // Check for lowercase letter
        if (requireLowercase && !password.chars().anyMatch(Character::isLowerCase)) {
            errorMessage.append("Password must contain at least one lowercase letter. ");
            isValid = false;
        }
        
        // Check for digit
        if (requireDigit && !password.chars().anyMatch(Character::isDigit)) {
            errorMessage.append("Password must contain at least one digit. ");
            isValid = false;
        }
        
        // Check for special character
        if (requireSpecialChar && !password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0)) {
            errorMessage.append("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?). ");
            isValid = false;
        }
        
        // Check for common weak patterns
        String lowerPassword = password.toLowerCase();
        if (lowerPassword.contains("password") || lowerPassword.contains("123456") || 
            lowerPassword.contains("qwerty") || lowerPassword.contains("admin")) {
            errorMessage.append("Password must not contain common weak patterns. ");
            isValid = false;
        }
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage.toString().trim())
                   .addConstraintViolation();
        }
        
        return isValid;
    }
}