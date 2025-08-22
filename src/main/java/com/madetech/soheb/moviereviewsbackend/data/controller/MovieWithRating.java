package com.madetech.soheb.moviereviewsbackend.data.controller;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.madetech.soheb.moviereviewsbackend.data.AgeRating;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
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

    public MovieWithRating(UUID id, String name, List<String> genres, List<String> directors, 
                          List<String> writers, List<String> cast, List<String> producers, 
                          Integer releaseYear, AgeRating ageRating, LocalDateTime createdAt, 
                          Double averageRating) {
        this.id = id;
        this.name = name;
        this.genres = genres;
        this.directors = directors;
        this.writers = writers;
        this.cast = cast;
        this.producers = producers;
        this.releaseYear = releaseYear;
        this.ageRating = ageRating;
        this.createdAt = createdAt;
        this.averageRating = averageRating;
    }

    public MovieWithRating(UUID id, String name, List<String> genres, List<String> directors, 
                          List<String> writers, List<String> cast, List<String> producers, 
                          Integer releaseYear, AgeRating ageRating, LocalDateTime createdAt, 
                          Object averageRating) {
        this(id, name, genres, directors, writers, cast, producers, releaseYear, ageRating, createdAt, 
             averageRating == null ? null : ((Number) averageRating).doubleValue());
    }

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