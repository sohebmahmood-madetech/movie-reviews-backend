package com.madetech.soheb.moviereviewsbackend.data.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private Long code;
    private String message;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiError apiError = (ApiError) o;
        return Objects.equals(code, apiError.code) &&
                Objects.equals(message, apiError.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }
}