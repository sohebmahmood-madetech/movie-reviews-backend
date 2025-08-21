package com.madetech.soheb.moviereviewsbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madetech.soheb.moviereviewsbackend.data.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Testcontainers
class MovieReviewsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Timeout(30)
    void fullUserJourney_SignupLoginSubmitReview_Success() throws Exception {
        // Step 1: User signup
        UserRegistrationRequest signupRequest = new UserRegistrationRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

        MvcResult signupResult = mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String signupResponse = signupResult.getResponse().getContentAsString();
        ApiResponse<?> signupApiResponse = objectMapper.readValue(signupResponse, ApiResponse.class);
        String jwtToken = (String) signupApiResponse.getResults();

        // Step 2: Submit a movie (using film auth token)
        MovieSubmissionRequest movieRequest = new MovieSubmissionRequest();
        movieRequest.setName("Test Movie");
        movieRequest.setGenres(List.of("Action", "Drama"));
        movieRequest.setDirectors(List.of("Test Director"));
        movieRequest.setWriters(List.of("Test Writer"));
        movieRequest.setCast(List.of("Test Actor"));
        movieRequest.setProducers(List.of("Test Producer"));
        movieRequest.setReleaseYear(2023);
        movieRequest.setAgeRating(AgeRating.BBFC_15);

        mockMvc.perform(post("/v1/movies/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieRequest))
                        .header("X-API-AUTH", "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4i5"))
                .andExpect(status().isOk());

        // Step 3: Get all movies
        MvcResult moviesResult = mockMvc.perform(get("/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Movie"))
                .andReturn();

        String moviesResponse = moviesResult.getResponse().getContentAsString();
        MovieWithRating[] movies = objectMapper.readValue(moviesResponse, MovieWithRating[].class);
        String movieId = movies[0].getId().toString();

        // Step 4: Submit a review
        ReviewSubmissionRequest reviewRequest = new ReviewSubmissionRequest();
        reviewRequest.setRating(8);
        reviewRequest.setDescription("Great movie!");

        mockMvc.perform(post("/v1/movies/" + movieId + "/review/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest))
                        .header("X-API-AUTH", jwtToken))
                .andExpect(status().isOk());

        // Step 5: Get movie reviews
        mockMvc.perform(get("/v1/movies/" + movieId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(8))
                .andExpect(jsonPath("$[0].description").value("Great movie!"));

        // Step 6: User login
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results").isNotEmpty());
    }

    @Test
    @Timeout(10)
    void movieSubmission_InvalidAuthToken_ReturnsUnauthorized() throws Exception {
        MovieSubmissionRequest request = new MovieSubmissionRequest();
        request.setName("Test Movie");
        request.setGenres(List.of("Action"));
        request.setDirectors(List.of("Test Director"));
        request.setWriters(List.of("Test Writer"));
        request.setCast(List.of("Test Actor"));
        request.setProducers(List.of("Test Producer"));
        request.setReleaseYear(2023);
        request.setAgeRating(AgeRating.BBFC_15);

        mockMvc.perform(post("/v1/movies/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-AUTH", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Timeout(10)
    void userRegistration_DuplicateUsername_ReturnsBadRequest() throws Exception {
        // First registration
        UserRegistrationRequest firstRequest = new UserRegistrationRequest();
        firstRequest.setUsername("duplicateuser");
        firstRequest.setEmail("first@example.com");
        firstRequest.setPassword("password123");
        firstRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Second registration with same username
        UserRegistrationRequest secondRequest = new UserRegistrationRequest();
        secondRequest.setUsername("duplicateuser");
        secondRequest.setEmail("second@example.com");
        secondRequest.setPassword("password123");
        secondRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}