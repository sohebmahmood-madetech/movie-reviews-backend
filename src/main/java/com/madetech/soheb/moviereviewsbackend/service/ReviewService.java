package com.madetech.soheb.moviereviewsbackend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.madetech.soheb.moviereviewsbackend.data.database.Movie;
import com.madetech.soheb.moviereviewsbackend.data.database.Review;
import com.madetech.soheb.moviereviewsbackend.data.exceptions.ReviewServiceException;
import com.madetech.soheb.moviereviewsbackend.data.controller.ReviewSubmissionRequest;
import com.madetech.soheb.moviereviewsbackend.data.database.User;
import com.madetech.soheb.moviereviewsbackend.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieService movieService;

    public ReviewService(ReviewRepository reviewRepository, MovieService movieService) {
        this.reviewRepository = reviewRepository;
        this.movieService = movieService;
    }

    public Optional<Review> submitReview(UUID movieId, ReviewSubmissionRequest request, User user) {
        return executeWithErrorHandling(
                () -> {
                    if (!movieService.movieExists(movieId)) {
                        return Optional.<Review>empty();
                    }

                    if (reviewRepository.existsByUser_IdAndMovie_Id(user.getId(), movieId)) {
                        return Optional.<Review>empty();
                    }

                    // Get the movie entity for proper relationship
                    Optional<Movie> movieOpt = movieService.findMovieById(movieId);
                    if (movieOpt.isEmpty()) {
                        return Optional.<Review>empty();
                    }

                    Review review = new Review();
                    review.setId(UuidCreator.getTimeOrderedEpoch());
                    review.setMovie(movieOpt.get());
                    review.setUser(user);
                    review.setRating(request.getRating());
                    review.setDescription(request.getDescription());
                    review.setTimestamp(LocalDateTime.now());

                    return Optional.of(reviewRepository.save(review));
                },
                "ERR_REVIEW_SUBMISSION_FAILED: Failed to submit review"
        );
    }

    public List<Review> getReviewsForMovie(UUID movieId) {
        return executeWithErrorHandling(
                () -> {
                    if (!movieService.movieExists(movieId)) {
                        return List.<Review>of();
                    }
                    return reviewRepository.findByMovie_IdOrderByTimestampDesc(movieId);
                },
                "ERR_REVIEWS_RETRIEVAL_FAILED: Failed to retrieve reviews for movie"
        );
    }

    public List<Review> getReviewsByUser(UUID userId) {
        return executeWithErrorHandling(
                () -> reviewRepository.findByUser_IdOrderByTimestampDesc(userId),
                "ERR_USER_REVIEWS_RETRIEVAL_FAILED: Failed to retrieve reviews by user"
        );
    }

    private <T> T executeWithErrorHandling(Supplier<T> operation, String errorMessage) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error(errorMessage, e);
            String errorCode = errorMessage.split(":")[0];
            throw new ReviewServiceException(errorCode, errorCode.replace("ERR_", "").replace("_", " ").toLowerCase(), e);
        }
    }
}