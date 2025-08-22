package com.madetech.soheb.moviereviewsbackend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.madetech.soheb.moviereviewsbackend.data.database.User;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserRegistrationRequest;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserLoginRequest;
import com.madetech.soheb.moviereviewsbackend.data.exceptions.AuthenticationException;
import com.madetech.soheb.moviereviewsbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FilmTokenAuthenticationService filmTokenAuthenticationService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            FilmTokenAuthenticationService filmTokenAuthenticationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.filmTokenAuthenticationService = filmTokenAuthenticationService;
    }


    public Optional<User> registerUser(UserRegistrationRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return Optional.empty();
            }

            if (userRepository.existsByEmail(request.getEmail())) {
                return Optional.empty();
            }

            User user = new User();
            user.setId(UuidCreator.getTimeOrderedEpoch());
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setDateOfBirth(request.getDateOfBirth());
            user.setRejected(false);
            user.setCreatedAt(LocalDateTime.now());

            return Optional.of(userRepository.save(user));
        } catch (Exception e) {
            logger.error("ERR_USER_REGISTRATION_FAILED: Failed to register user", e);
            throw new AuthenticationException("ERR_USER_REGISTRATION_FAILED", "User registration failed");
        }
    }

    public Optional<User> authenticateUser(UserLoginRequest request) {
        try {
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail());
            
            if (userOpt.isEmpty()) {
                return Optional.empty();
            }

            User user = userOpt.get();
            
            if (user.isRejected()) {
                return Optional.empty();
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                return Optional.empty();
            }

            return Optional.of(user);
        } catch (Exception e) {
            logger.error("ERR_USER_AUTHENTICATION_FAILED: Failed to authenticate user", e);
            throw new AuthenticationException("ERR_USER_AUTHENTICATION_FAILED", "User authentication failed");
        }
    }

    public String generateJwtToken(User user) {
        try {
            return jwtService.generateToken(user.getId());
        } catch (Exception e) {
            logger.error("ERR_JWT_GENERATION_FAILED: Failed to generate JWT token", e);
            throw new AuthenticationException("ERR_JWT_GENERATION_FAILED", "JWT token generation failed");
        }
    }

    public Optional<User> validateJwtToken(String token) {
        try {
            if (!jwtService.isTokenValid(token)) {
                return Optional.empty();
            }

            UUID userId = jwtService.extractUserId(token);
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty() || userOpt.get().isRejected()) {
                return Optional.empty();
            }

            return userOpt;
        } catch (Exception e) {
            logger.error("ERR_JWT_VALIDATION_FAILED: Failed to validate JWT token", e);
            return Optional.empty();
        }
    }

    public boolean validateFilmToken(String token) {
        try {
            return filmTokenAuthenticationService.isValidToken(token);
        } catch (Exception e) {
            logger.error("ERR_FILM_TOKEN_VALIDATION_FAILED: Failed to validate film token", e);
            return false;
        }
    }

}