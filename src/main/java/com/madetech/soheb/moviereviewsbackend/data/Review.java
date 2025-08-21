package com.madetech.soheb.moviereviewsbackend.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    private UUID id;
    
    @Column(name = "movie_id", nullable = false)
    private UUID movieId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id) &&
                Objects.equals(movieId, review.movieId) &&
                Objects.equals(userId, review.userId) &&
                Objects.equals(rating, review.rating) &&
                Objects.equals(description, review.description) &&
                Objects.equals(timestamp, review.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, movieId, userId, rating, description, timestamp);
    }
}