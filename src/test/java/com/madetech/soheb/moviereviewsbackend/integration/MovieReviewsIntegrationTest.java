package com.madetech.soheb.moviereviewsbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madetech.soheb.moviereviewsbackend.config.TestSecurityConfig;
import com.madetech.soheb.moviereviewsbackend.controller.AuthController;
import com.madetech.soheb.moviereviewsbackend.controller.MovieController;
import com.madetech.soheb.moviereviewsbackend.data.AgeRating;
import com.madetech.soheb.moviereviewsbackend.data.controller.ApiResponse;
import com.madetech.soheb.moviereviewsbackend.data.controller.MovieSubmissionRequest;
import com.madetech.soheb.moviereviewsbackend.data.controller.MovieWithRating;
import com.madetech.soheb.moviereviewsbackend.data.controller.ReviewSubmissionRequest;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserLoginRequest;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserRegistrationRequest;
import com.madetech.soheb.moviereviewsbackend.data.database.Movie;
import com.madetech.soheb.moviereviewsbackend.data.database.Review;
import com.madetech.soheb.moviereviewsbackend.data.database.User;
import com.madetech.soheb.moviereviewsbackend.service.AuthenticationService;
import com.madetech.soheb.moviereviewsbackend.service.FilmTokenAuthenticationService;
import com.madetech.soheb.moviereviewsbackend.service.MovieService;
import com.madetech.soheb.moviereviewsbackend.service.ReviewService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AuthController.class, MovieController.class})
@Import(TestSecurityConfig.class)
class MovieReviewsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AuthenticationService authenticationService;
    
    @MockitoBean
    private MovieService movieService;
    
    @MockitoBean
    private ReviewService reviewService;

    @Test
    @Timeout(30)
    void fullUserJourney_SignupLoginSubmitReview_Success() throws Exception {
        // Create test data
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        
        User testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testUser.setRejected(false);
        
        Movie testMovie = new Movie();
        testMovie.setId(movieId);
        testMovie.setName("Test Movie");
        testMovie.setGenres(List.of("Action", "Drama"));
        testMovie.setDirectors(List.of("Test Director"));
        testMovie.setWriters(List.of("Test Writer"));
        testMovie.setCast(List.of("Test Actor"));
        testMovie.setProducers(List.of("Test Producer"));
        testMovie.setReleaseYear(2023);
        testMovie.setAgeRating(AgeRating.BBFC_15);
        testMovie.setCreatedAt(LocalDateTime.now());
        
        Review testReview = new Review();
        testReview.setId(reviewId);
        testReview.setMovie(testMovie);
        testReview.setUser(testUser);
        testReview.setRating(8);
        testReview.setDescription("Great movie!");
        testReview.setTimestamp(LocalDateTime.now());
        
        MovieWithRating movieWithRating = new MovieWithRating(
                movieId, "Test Movie", List.of("Action", "Drama"), List.of("Test Director"),
                List.of("Test Writer"), List.of("Test Actor"), List.of("Test Producer"),
                2023, AgeRating.BBFC_15, LocalDateTime.now(), null
        );

        // Step 1: User signup
        UserRegistrationRequest signupRequest = createUserRegistrationRequest();
        
        // Mock successful registration
        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(Optional.of(testUser));
        
        when(authenticationService.generateJwtToken(testUser))
                .thenReturn("test-jwt-token");

        MvcResult signupResult = mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results").value("test-jwt-token"))
                .andReturn();

        // Step 2: Submit a movie
        when(movieService.submitMovie(any(MovieSubmissionRequest.class)))
                .thenReturn(Optional.of(testMovie));

        mockMvc.perform(post("/v1/movies/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMovieSubmissionRequest()))
                        .header("X-API-AUTH", "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4i5"))
                .andExpect(status().isOk());

        // Step 3: Get all movies
        when(movieService.getAllMoviesWithRating()).thenReturn(List.of(movieWithRating));

        mockMvc.perform(get("/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Movie"));

        // Step 4: Submit a review
        when(reviewService.submitReview(eq(movieId), any(ReviewSubmissionRequest.class), any(User.class)))
                .thenReturn(Optional.of(testReview));

        mockMvc.perform(post("/v1/movies/" + movieId + "/review/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewSubmissionRequest()))
                        .header("X-API-AUTH", "test-jwt-token"))
                .andExpect(status().isOk());

        // Step 5: Get movie reviews
        when(reviewService.getReviewsForMovie(movieId)).thenReturn(List.of(testReview));

        mockMvc.perform(get("/v1/movies/" + movieId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(8))
                .andExpect(jsonPath("$[0].description").value("Great movie!"));

        // Step 6: User login
        when(authenticationService.authenticateUser(any(UserLoginRequest.class)))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserLoginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results").value("test-jwt-token"));
    }

    @Test
    @Timeout(10)
    void movieSubmission_InvalidAuthToken_ReturnsUnauthorized() throws Exception {
        // The TestSecurityConfig already mocks FilmTokenAuthenticationService to return false for "invalid-token"
        mockMvc.perform(post("/v1/movies/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createMovieSubmissionRequest()))
                        .header("X-API-AUTH", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Timeout(10)
    void userRegistration_DuplicateUsername_ReturnsBadRequest() throws Exception {
        User testUser = createTestUser();

        // First registration succeeds
        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(Optional.of(testUser));
        
        when(authenticationService.generateJwtToken(testUser))
                .thenReturn("test-jwt-token");

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRegistrationRequest())))
                .andExpect(status().isOk());

        // Reset mock for second call - return empty to simulate failure
        reset(authenticationService);
        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(Optional.empty()); // Simulate duplicate username

        UserRegistrationRequest duplicateRequest = createUserRegistrationRequest();
        duplicateRequest.setEmail("different@example.com");

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // Helper methods
    private UserRegistrationRequest createUserRegistrationRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("7g:T,um]_d\"dRlO9");  // Meets strong password requirements
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));  // User will be 35 years old, meets age requirement
        return request;
    }
    
    private UserLoginRequest createUserLoginRequest() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("7g:T,um]_d\"dRlO9");
        return request;
    }
    
    private MovieSubmissionRequest createMovieSubmissionRequest() {
        MovieSubmissionRequest request = new MovieSubmissionRequest();
        request.setName("Test Movie");
        request.setGenres(List.of("Action", "Drama"));
        request.setDirectors(List.of("Test Director"));
        request.setWriters(List.of("Test Writer"));
        request.setCast(List.of("Test Actor"));
        request.setProducers(List.of("Test Producer"));
        request.setReleaseYear(2023);
        request.setAgeRating(AgeRating.BBFC_15);
        return request;
    }
    
    private ReviewSubmissionRequest createReviewSubmissionRequest() {
        ReviewSubmissionRequest request = new ReviewSubmissionRequest();
        request.setRating(8);
        request.setDescription("Great movie!");
        return request;
    }
    
    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setRejected(false);
        return user;
    }
    
    private Movie createTestMovie() {
        Movie movie = new Movie();
        movie.setId(UUID.randomUUID());
        movie.setName("Test Movie");
        movie.setGenres(List.of("Action", "Drama"));
        movie.setDirectors(List.of("Test Director"));
        movie.setWriters(List.of("Test Writer"));
        movie.setCast(List.of("Test Actor"));
        movie.setProducers(List.of("Test Producer"));
        movie.setReleaseYear(2023);
        movie.setAgeRating(AgeRating.BBFC_15);
        movie.setCreatedAt(LocalDateTime.now());
        return movie;
    }
}