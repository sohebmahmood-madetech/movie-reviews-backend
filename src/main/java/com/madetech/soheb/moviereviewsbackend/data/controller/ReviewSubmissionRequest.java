package com.madetech.soheb.moviereviewsbackend.data.controller;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmissionRequest {
    @NotNull
    @Min(0)
    @Max(10)
    private Integer rating;

    @NotBlank
    @Size(max = 500)
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewSubmissionRequest that = (ReviewSubmissionRequest) o;
        return Objects.equals(rating, that.rating) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rating, description);
    }
}