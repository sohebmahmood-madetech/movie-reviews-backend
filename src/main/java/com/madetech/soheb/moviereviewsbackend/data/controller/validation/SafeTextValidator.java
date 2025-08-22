package com.madetech.soheb.moviereviewsbackend.data.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SafeTextValidator implements ConstraintValidator<SafeText, String> {
    
    // Pattern to detect potentially dangerous HTML/script content
    private static final Pattern UNSAFE_PATTERN = Pattern.compile(
        "(?i)(<script[^>]*>.*?</script>)|" +        // Script tags
        "(<iframe[^>]*>.*?</iframe>)|" +            // Iframe tags
        "(javascript:)|" +                          // JavaScript protocol
        "(on\\w+\\s*=)|" +                         // Event handlers (onclick, onload, etc.)
        "(<object[^>]*>.*?</object>)|" +           // Object tags
        "(<embed[^>]*>)|" +                        // Embed tags
        "(vbscript:)|" +                           // VBScript protocol
        "(<form[^>]*>)|" +                         // Form tags
        "(<input[^>]*>)|" +                        // Input tags
        "(data:text/html)|" +                      // Data URLs with HTML
        "(<meta[^>]*>)"                            // Meta tags
    );
    
    // Pattern for SQL injection attempts
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union\\s+select)|" +
        "(insert\\s+into)|" +
        "(delete\\s+from)|" +
        "(update\\s+\\w+\\s+set)|" +
        "(drop\\s+table)|" +
        "(drop\\s+database)|" +
        "('\\s*;)|" +
        "(--|#)|" +
        "(\\/\\*.*?\\*\\/)"
    );
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let @NotBlank handle empty values
        }
        
        // Check for unsafe HTML/script content
        if (UNSAFE_PATTERN.matcher(value).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Text contains potentially unsafe HTML or script content")
                   .addConstraintViolation();
            return false;
        }
        
        // Check for SQL injection attempts
        if (SQL_INJECTION_PATTERN.matcher(value).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Text contains potentially unsafe SQL content")
                   .addConstraintViolation();
            return false;
        }
        
        // Check for excessive special characters that might indicate malicious input
        long specialCharCount = value.chars()
            .filter(ch -> "<>&\"'\\".indexOf(ch) >= 0)
            .count();
        
        if (specialCharCount > value.length() * 0.1) { // More than 10% special chars
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Text contains too many special characters")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}