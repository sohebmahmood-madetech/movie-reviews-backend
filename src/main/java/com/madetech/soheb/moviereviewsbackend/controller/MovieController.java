package com.madetech.soheb.moviereviewsbackend.controller;

import com.madetech.soheb.moviereviewsbackend.data.database.Movie;
import com.madetech.soheb.moviereviewsbackend.data.database.Review;
import com.madetech.soheb.moviereviewsbackend.data.database.User;
import com.madetech.soheb.moviereviewsbackend.data.controller.MovieSubmissionRequest;
import com.madetech.soheb.moviereviewsbackend.data.controller.MovieWithRating;
import com.madetech.soheb.moviereviewsbackend.data.controller.ReviewSubmissionRequest;
import com.madetech.soheb.moviereviewsbackend.service.AuthenticationService;
import com.madetech.soheb.moviereviewsbackend.service.MovieService;
import com.madetech.soheb.moviereviewsbackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/movies")
public class MovieController {

    private final MovieService movieService;
    private final ReviewService reviewService;
    private final AuthenticationService authenticationService;

    public MovieController(MovieService movieService, ReviewService reviewService, 
                          AuthenticationService authenticationService) {
        this.movieService = movieService;
        this.reviewService = reviewService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Void> submitMovie(@Valid @RequestBody MovieSubmissionRequest request,
                                          BindingResult bindingResult,
                                          @RequestHeader("X-API-AUTH") String authToken) {
        try {
            if (!authenticationService.validateFilmToken(authToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Movie> movieOpt = movieService.submitMovie(request);
            
            if (movieOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            return ResponseEntity.ok().build();
            
        } catch (RuntimeException e) {
            log.error("Movie submission failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<MovieWithRating>> getAllMovies() {
        try {
            List<MovieWithRating> movies = movieService.getAllMoviesWithRating();
            return ResponseEntity.ok(movies);
            
        } catch (RuntimeException e) {
            log.error("Failed to retrieve movies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{movieId}/review/submit")
    public ResponseEntity<Void> submitReview(@PathVariable UUID movieId,
                                           @Valid @RequestBody ReviewSubmissionRequest request,
                                           BindingResult bindingResult,
                                           @RequestHeader("X-API-AUTH") String authToken) {
        try {
            Optional<User> userOpt = authenticationService.validateJwtToken(authToken);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Review> reviewOpt = reviewService.submitReview(movieId, request, userOpt.get());
            
            if (reviewOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok().build();
            
        } catch (RuntimeException e) {
            log.error("Review submission failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{movieId}/reviews")
    public ResponseEntity<List<Review>> getMovieReviews(@PathVariable UUID movieId) {
        try {
            List<Review> reviews = reviewService.getReviewsForMovie(movieId);
            return ResponseEntity.ok(reviews);
            
        } catch (RuntimeException e) {
            log.error("Failed to retrieve movie reviews", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}