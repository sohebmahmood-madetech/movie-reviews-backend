package com.madetech.soheb.moviereviewsbackend.repository;

import com.madetech.soheb.moviereviewsbackend.data.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByMovieIdOrderByTimestampDesc(UUID movieId);

    List<Review> findByUserIdOrderByTimestampDesc(UUID userId);

    boolean existsByUserIdAndMovieId(UUID userId, UUID movieId);
}