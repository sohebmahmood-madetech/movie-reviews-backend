package com.madetech.soheb.moviereviewsbackend.repository;

import com.madetech.soheb.moviereviewsbackend.data.database.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByMovie_IdOrderByTimestampDesc(UUID movieId);

    List<Review> findByUser_IdOrderByTimestampDesc(UUID userId);

    boolean existsByUser_IdAndMovie_Id(UUID userId, UUID movieId);
}