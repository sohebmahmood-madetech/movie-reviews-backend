package com.madetech.soheb.moviereviewsbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madetech.soheb.moviereviewsbackend.data.*;
import com.madetech.soheb.moviereviewsbackend.service.AuthenticationService;
import com.madetech.soheb.moviereviewsbackend.service.MovieService;
import com.madetech.soheb.moviereviewsbackend.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = MovieController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Timeout(5)
    void submitMovie_ValidRequestAndToken_ReturnsOk() throws Exception {
        MovieSubmissionRequest request = new MovieSubmissionRequest();
        request.setName("Test Movie");
        request.setGenres(List.of("Action"));
        request.setDirectors(List.of("Test Director"));
        request.setWriters(List.of("Test Writer"));
        request.setCast(List.of("Test Actor"));
        request.setProducers(List.of("Test Producer"));
        request.setReleaseYear(2023);
        request.setAgeRating(AgeRating.BBFC_15);

        Movie movie = new Movie();
        movie.setId(UUID.randomUUID());
        movie.setName("Test Movie");

        when(authenticationService.validateFilmToken("validtoken")).thenReturn(true);
        when(movieService.submitMovie(any(MovieSubmissionRequest.class))).thenReturn(Optional.of(movie));

        mockMvc.perform(post("/v1/movies/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-AUTH", "validtoken"))
                .andExpect(status().isOk());
    }

    @Test
    @Timeout(5)
    void submitMovie_InvalidToken_ReturnsUnauthorized() throws Exception {
        MovieSubmissionRequest request = new MovieSubmissionRequest();
        request.setName("Test Movie");
        request.setGenres(List.of("Action"));
        request.setDirectors(List.of("Test Director"));
        request.setWriters(List.of("Test Writer"));
        request.setCast(List.of("Test Actor"));
        request.setProducers(List.of("Test Producer"));
        request.setReleaseYear(2023);
        request.setAgeRating(AgeRating.BBFC_15);

        when(authenticationService.validateFilmToken("invalidtoken")).thenReturn(false);

        mockMvc.perform(post("/v1/movies/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-AUTH", "invalidtoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Timeout(5)
    void submitMovie_InvalidRequest_ReturnsBadRequest() throws Exception {
        MovieSubmissionRequest request = new MovieSubmissionRequest();
        request.setName("");
        request.setGenres(List.of());
        request.setDirectors(List.of());
        request.setWriters(List.of());
        request.setCast(List.of());
        request.setProducers(List.of());

        when(authenticationService.validateFilmToken("validtoken")).thenReturn(true);

        mockMvc.perform(post("/v1/movies/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-AUTH", "validtoken"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Timeout(5)
    void getAllMovies_ReturnsMovieList() throws Exception {
        MovieWithRating movie1 = new MovieWithRating();
        movie1.setId(UUID.randomUUID());
        movie1.setName("Movie 1");
        movie1.setAverageRating(8.5);

        MovieWithRating movie2 = new MovieWithRating();
        movie2.setId(UUID.randomUUID());
        movie2.setName("Movie 2");
        movie2.setAverageRating(7.2);

        List<MovieWithRating> movies = Arrays.asList(movie1, movie2);
        when(movieService.getAllMoviesWithRating()).thenReturn(movies);

        mockMvc.perform(get("/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Movie 1"))
                .andExpect(jsonPath("$[0].averageRating").value(8.5))
                .andExpect(jsonPath("$[1].name").value("Movie 2"))
                .andExpect(jsonPath("$[1].averageRating").value(7.2));
    }

    @Test
    @Timeout(5)
    void submitReview_ValidRequestAndToken_ReturnsOk() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        ReviewSubmissionRequest request = new ReviewSubmissionRequest();
        request.setRating(8);
        request.setDescription("Great movie!");

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setMovieId(movieId);
        review.setUserId(userId);

        when(authenticationService.validateJwtToken("validjwt")).thenReturn(Optional.of(user));
        when(reviewService.submitReview(eq(movieId), any(ReviewSubmissionRequest.class), eq(user)))
                .thenReturn(Optional.of(review));

        mockMvc.perform(post("/v1/movies/" + movieId + "/review/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-AUTH", "validjwt"))
                .andExpect(status().isOk());
    }

    @Test
    @Timeout(5)
    void submitReview_InvalidToken_ReturnsUnauthorized() throws Exception {
        UUID movieId = UUID.randomUUID();
        
        ReviewSubmissionRequest request = new ReviewSubmissionRequest();
        request.setRating(8);
        request.setDescription("Great movie!");

        when(authenticationService.validateJwtToken("invalidjwt")).thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/movies/" + movieId + "/review/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-AUTH", "invalidjwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Timeout(5)
    void submitReview_InvalidRequest_ReturnsBadRequest() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        ReviewSubmissionRequest request = new ReviewSubmissionRequest();
        request.setRating(-1);
        request.setDescription("");

        User user = new User();
        user.setId(userId);

        when(authenticationService.validateJwtToken("validjwt")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/v1/movies/" + movieId + "/review/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-AUTH", "validjwt"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Timeout(5)
    void getMovieReviews_ReturnsReviewList() throws Exception {
        UUID movieId = UUID.randomUUID();
        
        Review review1 = new Review();
        review1.setId(UUID.randomUUID());
        review1.setMovieId(movieId);
        review1.setRating(8);
        review1.setDescription("Great movie!");
        review1.setTimestamp(LocalDateTime.now());
        
        Review review2 = new Review();
        review2.setId(UUID.randomUUID());
        review2.setMovieId(movieId);
        review2.setRating(6);
        review2.setDescription("Ok movie");
        review2.setTimestamp(LocalDateTime.now());

        List<Review> reviews = Arrays.asList(review1, review2);
        when(reviewService.getReviewsForMovie(movieId)).thenReturn(reviews);

        mockMvc.perform(get("/v1/movies/" + movieId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(8))
                .andExpect(jsonPath("$[0].description").value("Great movie!"))
                .andExpect(jsonPath("$[1].rating").value(6))
                .andExpect(jsonPath("$[1].description").value("Ok movie"));
    }
}