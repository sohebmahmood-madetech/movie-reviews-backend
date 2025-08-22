package com.madetech.soheb.moviereviewsbackend.data.controller.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidAgeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAge {
    String message() default "User must be at least 13 years old";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int minAge() default 13;
    int maxAge() default 120;
}