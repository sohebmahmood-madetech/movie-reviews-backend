package com.madetech.soheb.moviereviewsbackend.data.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.madetech.soheb.moviereviewsbackend.data.AgeRating;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieWithRating {
    private UUID id;
    private String name;
    private List<String> genres;
    private List<String> directors;
    private List<String> writers;
    private List<String> cast;
    private List<String> producers;
    private Integer releaseYear;
    private AgeRating ageRating;
    private LocalDateTime createdAt;
    private Double averageRating;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieWithRating that = (MovieWithRating) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(genres, that.genres) &&
                Objects.equals(directors, that.directors) &&
                Objects.equals(writers, that.writers) &&
                Objects.equals(cast, that.cast) &&
                Objects.equals(producers, that.producers) &&
                Objects.equals(releaseYear, that.releaseYear) &&
                ageRating == that.ageRating &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(averageRating, that.averageRating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, genres, directors, writers, cast, producers, releaseYear, ageRating, createdAt, averageRating);
    }
}