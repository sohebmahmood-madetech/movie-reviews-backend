package com.madetech.soheb.moviereviewsbackend.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    private UUID id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre", length = 20)
    private List<String> genres;
    
    @ElementCollection
    @CollectionTable(name = "movie_directors", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "director", length = 100)
    private List<String> directors;
    
    @ElementCollection
    @CollectionTable(name = "movie_writers", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "writer", length = 100)
    private List<String> writers;
    
    @ElementCollection
    @CollectionTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "cast_member", length = 100)
    private List<String> cast;
    
    @ElementCollection
    @CollectionTable(name = "movie_producers", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "producer", length = 100)
    private List<String> producers;
    
    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating", nullable = false)
    private AgeRating ageRating;
    
    @Column(name = "created_at", nullable = false)
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