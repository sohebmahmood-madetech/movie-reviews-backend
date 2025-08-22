package com.madetech.soheb.moviereviewsbackend.data.controller;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.madetech.soheb.moviereviewsbackend.data.AgeRating;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieSubmissionRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Size(min = 1)
    private List<@Size(max = 20) @NotBlank String> genres;

    @NotNull
    @Size(min = 1)
    private List<@Size(max = 100) @NotBlank String> directors;

    @NotNull
    @Size(min = 1)
    private List<@Size(max = 100) @NotBlank String> writers;

    @NotNull
    @Size(min = 1)
    private List<@Size(max = 100) @NotBlank String> cast;

    @NotNull
    @Size(min = 1)
    private List<@Size(max = 100) @NotBlank String> producers;

    @NotNull
    @Min(1900)
    @Max(2200)
    private Integer releaseYear;

    @NotNull
    private AgeRating ageRating;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieSubmissionRequest that = (MovieSubmissionRequest) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(genres, that.genres) &&
                Objects.equals(directors, that.directors) &&
                Objects.equals(writers, that.writers) &&
                Objects.equals(cast, that.cast) &&
                Objects.equals(producers, that.producers) &&
                Objects.equals(releaseYear, that.releaseYear) &&
                ageRating == that.ageRating;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, genres, directors, writers, cast, producers, releaseYear, ageRating);
    }
}