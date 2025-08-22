package com.madetech.soheb.moviereviewsbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.madetech.soheb.moviereviewsbackend.data.database.User;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserRegistrationRequest;
import com.madetech.soheb.moviereviewsbackend.data.controller.UserLoginRequest;
import com.madetech.soheb.moviereviewsbackend.data.exceptions.AuthenticationException;
import com.madetech.soheb.moviereviewsbackend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;

@Slf4j
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceLoader resourceLoader;
    private final PrivateKey jwtPrivateKey;
    private final PublicKey jwtPublicKey;
    private final ObjectMapper objectMapper;
    private final List<FilmAuthToken> filmAuthTokens;

    public AuthenticationService(
            UserRepository userRepository,
            ResourceLoader resourceLoader,
            PasswordEncoder passwordEncoder,
            @Value("${moviereviews.auth.review.privatekey}") String privateKeyString,
            @Value("${moviereviews.auth.review.publickey}") String publicKeyString) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resourceLoader = resourceLoader;
        this.jwtPrivateKey = parsePrivateKey(privateKeyString);
        this.jwtPublicKey = parsePublicKey(publicKeyString);
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
            user.setCreatedAt(LocalDateTime.now());

            return Optional.of(userRepository.save(user));
        } catch (Exception e) {
            log.error("ERR_USER_REGISTRATION_FAILED: Failed to register user", e);
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
            log.error("ERR_USER_AUTHENTICATION_FAILED: Failed to authenticate user", e);
            throw new AuthenticationException("ERR_USER_AUTHENTICATION_FAILED", "User authentication failed");
        }
    }

    public String generateJwtToken(User user) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000L); // 30 days

            return Jwts.builder()
                    .subject(user.getId().toString())
                    .claim("username", user.getUsername())
                    .claim("email", user.getEmail())
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(jwtPrivateKey)
                    .compact();
        } catch (Exception e) {
            log.error("ERR_JWT_GENERATION_FAILED: Failed to generate JWT token", e);
            throw new AuthenticationException("ERR_JWT_GENERATION_FAILED", "JWT token generation failed");
        }
    }

    public Optional<User> validateJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtPublicKey)
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

    private PrivateKey parsePrivateKey(String privateKeyString) {
        try {
            String keyContent;
            
            // Check if it's already a PEM format or base64 encoded PEM
            if (privateKeyString.contains("-----BEGIN PRIVATE KEY-----")) {
                // Standard PEM format
                keyContent = privateKeyString
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
            } else {
                // Base64 encoded PEM - decode first
                String decodedPem = new String(Base64.getDecoder().decode(privateKeyString));
                keyContent = decodedPem
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
            }
            
            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("ERR_PRIVATE_KEY_PARSE_FAILED: Failed to parse private key", e);
            throw new AuthenticationException("ERR_PRIVATE_KEY_PARSE_FAILED", "Private key parsing failed");
        }
    }

    private PublicKey parsePublicKey(String publicKeyString) {
        try {
            String keyContent;
            
            // Check if it's already a PEM format or base64 encoded PEM
            if (publicKeyString.contains("-----BEGIN PUBLIC KEY-----")) {
                // Standard PEM format
                keyContent = publicKeyString
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");
            } else {
                // Base64 encoded PEM - decode first
                String decodedPem = new String(Base64.getDecoder().decode(publicKeyString));
                keyContent = decodedPem
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");
            }
            
            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("ERR_PUBLIC_KEY_PARSE_FAILED: Failed to parse public key", e);
            throw new AuthenticationException("ERR_PUBLIC_KEY_PARSE_FAILED", "Public key parsing failed");
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