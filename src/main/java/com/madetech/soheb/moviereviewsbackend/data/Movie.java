package com.madetech.soheb.moviereviewsbackend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id) &&
                Objects.equals(name, movie.name) &&
                Objects.equals(genres, movie.genres) &&
                Objects.equals(directors, movie.directors) &&
                Objects.equals(writers, movie.writers) &&
                Objects.equals(cast, movie.cast) &&
                Objects.equals(producers, movie.producers) &&
                Objects.equals(releaseYear, movie.releaseYear) &&
                ageRating == movie.ageRating &&
                Objects.equals(createdAt, movie.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, genres, directors, writers, cast, producers, releaseYear, ageRating, createdAt);
    }
}