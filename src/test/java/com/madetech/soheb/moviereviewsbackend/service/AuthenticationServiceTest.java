package com.madetech.soheb.moviereviewsbackend.service;

import com.madetech.soheb.moviereviewsbackend.data.database.User;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserLoginRequest;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserRegistrationRequest;
import com.madetech.soheb.moviereviewsbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private FilmTokenAuthenticationService filmTokenAuthenticationService;

    private AuthenticationService authenticationService;


    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService, filmTokenAuthenticationService);
    }

    @Test
    @Timeout(5)
    void registerUser_ValidRequest_ReturnsUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = authenticationService.registerUser(request);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
        assertFalse(result.get().isRejected());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @Timeout(5)
    void registerUser_UsernameExists_ReturnsEmpty() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("existinguser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        Optional<User> result = authenticationService.registerUser(request);

        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    @Timeout(5)
    void registerUser_EmailExists_ReturnsEmpty() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        Optional<User> result = authenticationService.registerUser(request);

        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    @Timeout(5)
    void authenticateUser_ValidCredentials_ReturnsUser() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password123");

        String encodedPassword = "hashedPassword";

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(encodedPassword);
        user.setRejected(false);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        Optional<User> result = authenticationService.authenticateUser(request);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    @Timeout(5)
    void authenticateUser_UserNotFound_ReturnsEmpty() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsernameOrEmail("nonexistent");
        request.setPassword("password123");

        when(userRepository.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = authenticationService.authenticateUser(request);

        assertFalse(result.isPresent());
    }

    @Test
    @Timeout(5)
    void authenticateUser_RejectedUser_ReturnsEmpty() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRejected(true);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = authenticationService.authenticateUser(request);

        assertFalse(result.isPresent());
    }

    @Test
    @Timeout(5)
    void generateJwtToken_ValidUser_ReturnsToken() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        String expectedToken = "test.jwt.token";
        when(jwtService.generateToken(user.getId())).thenReturn(expectedToken);

        String token = authenticationService.generateJwtToken(user);

        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(jwtService).generateToken(user.getId());
    }

    @Test
    @Timeout(5)
    void validateFilmToken_ValidToken_ReturnsTrue() {
        when(filmTokenAuthenticationService.isValidToken("testtoken123456789")).thenReturn(true);

        boolean result = authenticationService.validateFilmToken("testtoken123456789");

        assertTrue(result);
        verify(filmTokenAuthenticationService).isValidToken("testtoken123456789");
    }

    @Test
    @Timeout(5)
    void validateFilmToken_InvalidToken_ReturnsFalse() {
        when(filmTokenAuthenticationService.isValidToken("invalidtoken")).thenReturn(false);

        boolean result = authenticationService.validateFilmToken("invalidtoken");

        assertFalse(result);
        verify(filmTokenAuthenticationService).isValidToken("invalidtoken");
    }

    @Test
    @Timeout(5)
    void validateFilmToken_NullToken_ReturnsFalse() {
        when(filmTokenAuthenticationService.isValidToken(null)).thenReturn(false);

        boolean result = authenticationService.validateFilmToken(null);

        assertFalse(result);
        verify(filmTokenAuthenticationService).isValidToken(null);
    }
}