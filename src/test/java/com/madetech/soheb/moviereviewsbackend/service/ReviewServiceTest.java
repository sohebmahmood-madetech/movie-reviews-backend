package com.madetech.soheb.moviereviewsbackend.service;

import com.madetech.soheb.moviereviewsbackend.data.Review;
import com.madetech.soheb.moviereviewsbackend.data.ReviewSubmissionRequest;
import com.madetech.soheb.moviereviewsbackend.data.User;
import com.madetech.soheb.moviereviewsbackend.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieService movieService;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, movieService);
    }

    @Test
    @Timeout(5)
    void submitReview_ValidRequest_ReturnsReview() {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        
        ReviewSubmissionRequest request = new ReviewSubmissionRequest();
        request.setRating(8);
        request.setDescription("Great movie!");

        when(movieService.movieExists(movieId)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndMovieId(userId, movieId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Review> result = reviewService.submitReview(movieId, request, user);

        assertTrue(result.isPresent());
        assertEquals(movieId, result.get().getMovieId());
        assertEquals(userId, result.get().getUserId());
        assertEquals(8, result.get().getRating());
        assertEquals("Great movie!", result.get().getDescription());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @Timeout(5)
    void submitReview_MovieDoesNotExist_ReturnsEmpty() {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        User user = new User();
        user.setId(userId);
        
        ReviewSubmissionRequest request = new ReviewSubmissionRequest();
        request.setRating(8);
        request.setDescription("Great movie!");

        when(movieService.movieExists(movieId)).thenReturn(false);

        Optional<Review> result = reviewService.submitReview(movieId, request, user);

        assertFalse(result.isPresent());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @Timeout(5)
    void submitReview_UserAlreadyReviewed_ReturnsEmpty() {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        User user = new User();
        user.setId(userId);
        
        ReviewSubmissionRequest request = new ReviewSubmissionRequest();
        request.setRating(8);
        request.setDescription("Great movie!");

        when(movieService.movieExists(movieId)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndMovieId(userId, movieId)).thenReturn(true);

        Optional<Review> result = reviewService.submitReview(movieId, request, user);

        assertFalse(result.isPresent());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @Timeout(5)
    void getReviewsForMovie_ExistingMovie_ReturnsReviews() {
        UUID movieId = UUID.randomUUID();
        
        Review review1 = new Review();
        review1.setId(UUID.randomUUID());
        review1.setMovieId(movieId);
        review1.setRating(8);
        review1.setTimestamp(LocalDateTime.now());
        
        Review review2 = new Review();
        review2.setId(UUID.randomUUID());
        review2.setMovieId(movieId);
        review2.setRating(6);
        review2.setTimestamp(LocalDateTime.now());

        List<Review> reviews = Arrays.asList(review1, review2);

        when(movieService.movieExists(movieId)).thenReturn(true);
        when(reviewRepository.findByMovieId(movieId)).thenReturn(reviews);

        List<Review> result = reviewService.getReviewsForMovie(movieId);

        assertEquals(2, result.size());
        assertEquals(8, result.get(0).getRating());
        assertEquals(6, result.get(1).getRating());
        verify(reviewRepository).findByMovieId(movieId);
    }

    @Test
    @Timeout(5)
    void getReviewsForMovie_NonExistingMovie_ReturnsEmptyList() {
        UUID movieId = UUID.randomUUID();

        when(movieService.movieExists(movieId)).thenReturn(false);

        List<Review> result = reviewService.getReviewsForMovie(movieId);

        assertTrue(result.isEmpty());
        verify(reviewRepository, never()).findByMovieId(movieId);
    }

    @Test
    @Timeout(5)
    void getReviewsByUser_ReturnsUserReviews() {
        UUID userId = UUID.randomUUID();
        
        Review review1 = new Review();
        review1.setId(UUID.randomUUID());
        review1.setUserId(userId);
        review1.setRating(9);
        review1.setTimestamp(LocalDateTime.now());
        
        Review review2 = new Review();
        review2.setId(UUID.randomUUID());
        review2.setUserId(userId);
        review2.setRating(7);
        review2.setTimestamp(LocalDateTime.now());

        List<Review> reviews = Arrays.asList(review1, review2);
        when(reviewRepository.findByUserId(userId)).thenReturn(reviews);

        List<Review> result = reviewService.getReviewsByUser(userId);

        assertEquals(2, result.size());
        assertEquals(9, result.get(0).getRating());
        assertEquals(7, result.get(1).getRating());
        verify(reviewRepository).findByUserId(userId);
    }

    @Test
    @Timeout(5)
    void submitReview_RepositoryThrowsException_ThrowsRuntimeException() {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        User user = new User();
        user.setId(userId);
        
        ReviewSubmissionRequest request = new ReviewSubmissionRequest();
        request.setRating(8);
        request.setDescription("Great movie!");

        when(movieService.movieExists(movieId)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndMovieId(userId, movieId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> reviewService.submitReview(movieId, request, user));
    }
}