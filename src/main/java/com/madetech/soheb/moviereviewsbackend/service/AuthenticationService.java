package com.madetech.soheb.moviereviewsbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.madetech.soheb.moviereviewsbackend.data.*;
import com.madetech.soheb.moviereviewsbackend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceLoader resourceLoader;
    private final Key jwtSigningKey;
    private final ObjectMapper objectMapper;
    private final List<FilmAuthToken> filmAuthTokens;

    public AuthenticationService(
            UserRepository userRepository,
            ResourceLoader resourceLoader,
            @Value("${moviereviews.auth.review.privatekey}") String jwtPrivateKey) {
        this.userRepository = userRepository;
        this.passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        this.resourceLoader = resourceLoader;
        this.jwtSigningKey = Keys.hmacShaKeyFor(jwtPrivateKey.getBytes(StandardCharsets.UTF_8));
        this.objectMapper = new ObjectMapper();
        this.filmAuthTokens = loadFilmAuthTokens();
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

            return Optional.of(userRepository.save(user));
        } catch (Exception e) {
            log.error("ERR_USER_REGISTRATION_FAILED: Failed to register user", e);
            throw new RuntimeException("ERR_USER_REGISTRATION_FAILED");
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
            log.error("ERR_USER_AUTHENTICATION_FAILED: Failed to authenticate user", e);
            throw new RuntimeException("ERR_USER_AUTHENTICATION_FAILED");
        }
    }

    public String generateJwtToken(User user) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000L); // 30 days

            return Jwts.builder()
                    .setSubject(user.getId().toString())
                    .claim("username", user.getUsername())
                    .claim("email", user.getEmail())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(jwtSigningKey)
                    .compact();
        } catch (Exception e) {
            log.error("ERR_JWT_GENERATION_FAILED: Failed to generate JWT token", e);
            throw new RuntimeException("ERR_JWT_GENERATION_FAILED");
        }
    }

    public Optional<User> validateJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) jwtSigningKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty() || userOpt.get().isRejected()) {
                return Optional.empty();
            }

            return userOpt;
        } catch (Exception e) {
            log.error("ERR_JWT_VALIDATION_FAILED: Failed to validate JWT token", e);
            return Optional.empty();
        }
    }

    public boolean validateFilmToken(String token) {
        try {
            return filmAuthTokens.stream()
                    .anyMatch(authToken -> Objects.equals(authToken.getToken(), token));
        } catch (Exception e) {
            log.error("ERR_FILM_TOKEN_VALIDATION_FAILED: Failed to validate film token", e);
            return false;
        }
    }

    private List<FilmAuthToken> loadFilmAuthTokens() {
        try {
            Resource resource = resourceLoader.getResource("classpath:auth.json");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(content, new TypeReference<List<FilmAuthToken>>() {});
        } catch (IOException e) {
            log.error("ERR_FILM_AUTH_TOKENS_LOAD_FAILED: Failed to load film auth tokens", e);
            return Collections.emptyList();
        }
    }

    private static class FilmAuthToken {
        private String user;
        private String description;
        private String token;

        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}