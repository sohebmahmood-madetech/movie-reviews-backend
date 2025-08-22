package com.madetech.soheb.moviereviewsbackend.data.database;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotNull
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @NotNull
    private Movie movie;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    
    @Column(nullable = false)
    @NotNull
    @Min(value = 0, message = "Rating must be between 0 and 10")
    @Max(value = 10, message = "Rating must be between 0 and 10")
    private Integer rating;
    
    @Column(nullable = false, length = 500)
    @NotBlank(message = "Review description is required")
    @Size(max = 500, message = "Review description cannot exceed 500 characters")
    private String description;
    
    @Column(nullable = false)
    @NotNull
    private LocalDateTime timestamp;

    // Convenience methods for backward compatibility
    public UUID getMovieId() {
        return movie != null ? movie.getId() : null;
    }

    public UUID getUserId() {
        return user != null ? user.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id) &&
                Objects.equals(movie != null ? movie.getId() : null, review.movie != null ? review.movie.getId() : null) &&
                Objects.equals(user != null ? user.getId() : null, review.user != null ? review.user.getId() : null) &&
                Objects.equals(rating, review.rating) &&
                Objects.equals(description, review.description) &&
                Objects.equals(timestamp, review.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, movie != null ? movie.getId() : null, user != null ? user.getId() : null, rating, description, timestamp);
    }
}