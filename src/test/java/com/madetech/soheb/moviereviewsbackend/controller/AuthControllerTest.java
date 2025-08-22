package com.madetech.soheb.moviereviewsbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madetech.soheb.moviereviewsbackend.config.TestSecurityConfig;
import com.madetech.soheb.moviereviewsbackend.data.database.User;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserLoginRequest;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserRegistrationRequest;
import com.madetech.soheb.moviereviewsbackend.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, 
                classes = {
                        com.madetech.soheb.moviereviewsbackend.config.SecurityConfig.class,
                        com.madetech.soheb.moviereviewsbackend.config.JwtProperties.class,
                        com.madetech.soheb.moviereviewsbackend.config.JwtAuthenticationFilter.class,
                        com.madetech.soheb.moviereviewsbackend.config.FilmTokenAuthenticationFilter.class,
                        com.madetech.soheb.moviereviewsbackend.config.RateLimitFilter.class,
                        com.madetech.soheb.moviereviewsbackend.config.SecurityHeadersFilter.class
                }
        )
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Timeout(5)
    @WithMockUser
    void signup_ValidRequest_ReturnsSuccess() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("StrongP@ss123!");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());

        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(Optional.of(user));
        when(authenticationService.generateJwtToken(user)).thenReturn("jwt.token.here");

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results").value("jwt.token.here"))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    @Timeout(5)
    @WithMockUser
    void signup_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("");
        request.setEmail("invalid-email");
        request.setPassword("StrongP@ss123!");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.results").isEmpty())
                .andExpect(jsonPath("$.error.code").value(1001L));
    }

    @Test
    @Timeout(5)
    @WithMockUser
    void signup_UserAlreadyExists_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("existinguser");
        request.setEmail("existing@example.com");
        request.setPassword("StrongP@ss123!");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.results").isEmpty())
                .andExpect(jsonPath("$.error.code").value(1002L));
    }

    @Test
    @Timeout(5)
    @WithMockUser
    void signup_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("StrongP@ss123!");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        when(authenticationService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.results").isEmpty())
                .andExpect(jsonPath("$.error.code").value(1003L));
    }

    @Test
    @Timeout(5)
    @WithMockUser
    void login_ValidCredentials_ReturnsSuccess() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("StrongP@ss123!");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        when(authenticationService.authenticateUser(any(UserLoginRequest.class)))
                .thenReturn(Optional.of(user));
        when(authenticationService.generateJwtToken(user)).thenReturn("jwt.token.here");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.results").value("jwt.token.here"))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    @Timeout(5)
    @WithMockUser
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("wrongpassword");

        when(authenticationService.authenticateUser(any(UserLoginRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.results").isEmpty())
                .andExpect(jsonPath("$.error.code").value(1005L));
    }

    @Test
    @Timeout(5)
    @WithMockUser
    void login_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsernameOrEmail("");
        request.setPassword("");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.results").isEmpty())
                .andExpect(jsonPath("$.error.code").value(1004L));
    }
}