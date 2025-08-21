package com.madetech.soheb.moviereviewsbackend.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private UUID id;
    private UUID movieId;
    private UUID userId;
    private Integer rating;
    private String description;
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