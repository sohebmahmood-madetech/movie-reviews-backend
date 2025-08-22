package com.madetech.soheb.moviereviewsbackend.data.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class ValidAgeValidator implements ConstraintValidator<ValidAge, LocalDate> {
    
    private int minAge;
    private int maxAge;
    
    @Override
    public void initialize(ValidAge constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
        this.maxAge = constraintAnnotation.maxAge();
    }
    
    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        if (dateOfBirth == null) {
            return true; // Let @NotNull handle null values
        }
        
        LocalDate now = LocalDate.now();
        
        // Check if date is not in the future
        if (dateOfBirth.isAfter(now)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Date of birth cannot be in the future")
                   .addConstraintViolation();
            return false;
        }
        
        int age = Period.between(dateOfBirth, now).getYears();
        
        if (age < minAge) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("User must be at least " + minAge + " years old")
                   .addConstraintViolation();
            return false;
        }
        
        if (age > maxAge) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Age cannot exceed " + maxAge + " years")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}