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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthenticationService authenticationService;

    private final String testJwtPrivateKey = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2Z0lCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktnd2dnU2tBZ0VBQW9JQkFRREEwYWJuK2xoVEtNTTUKWnFQaEZPcGVLNEV3djNoSW5XVVBkb00yWEdZTlFRN2o2VUhrdHhFNlJabXhxYjcxNTBpMitrTEVzREZEcWkxTgpUWVJsV2Y4QnFQblFsam0ycVVVOXNoT2cyTDBNb3VIVnRnTm9WT2ZGcFhGK3lqdEx6Y1RveVUvazYxNXFwejh5CkxGUUhBT3E5OUF3N0V1TmRnUVhkYmc5VjdNQ2ZJRXR1MXk4VkJyN3h3Qm1oK1FTVGxNOVlGWDJIVXBmRTVlV3oKdWQzdTVmemdydDQ1dk5sOUcya0VJdjFKQUtWdUZkaC9yYkEybzJBRFZCajdwVzRBOWVzbERxMUZnOWxvUjZZTwoyNGd1SFNqc3FwQUVGWnNma1dRVWpOa29FbXFUY01wNXNZSXhYbGdQczZxRXlUTUswZXVmMU9sdERDNnpzRWszCmlYTFBsa1JuQWdNQkFBRUNnZ0VCQUppclBXZkNPMGRJWDRuYUcvKy9lQm5jeHpNNWwvd3FSZmVUeUE4UVkzMlMKV0JMRGRKcURWY2syNjM1ZkNKdURZUVNJWHB2cUwyRVFKOHdPTEFPV1VGY2tyeW5OcW4vR0pPR21YcTNqZkJSTwpJMjQvQ0pWcUJpODJJOWtTM2lPckJzRXdxb0diM2NaTGpGeTZlTjVtRXczcmJqS2ZFbHhtdWEwOGJXZE5iaWpHCjJVcGtaVEJpQnZpL3lKaWQ1N0YxV2FOWG5zUDZqeTNabEVhdUkrU1hGSHJ5YzJHUzJiN25GZWtKU3M1YW1CdGgKZTYyN3JrRWFtTnZpMGwzZG1pUU1OZUdCRWNzNnNadGNsSDdiUzNtTEczNUNCeGVXRlNHVEh2dXBCdWZ6YWlmKwo5SmNHOXo2T1o2clN1RGNxSjd5Y2l4MEFEWEhHdnZzNjkxUDVJcFpYRjAwQ2dZRUE5YlNFQlpyT2tLdWJSbXRZCkM5R21mVTYvQzBTRnprK1JLY1dCaDVoMHVWRmNlRHlwb01ncFp3NU0xMFdNSHFrUEp4WHpSNGEvUzZzd2pSbTAKTHNkNlZIeDZwVGVwclFlWHdHRXBIODNxUGRqaXJTNFNNb2U0T3NrMzMyMTY3dXpBOUpNRmdHYzNKL3hqUmlNVwpJUUdkWnZxZlhKSUlrVEMxMTZRZGJKY0hqSk1DZ1lFQTJ6N2lqakhlZmJMN2FjUlRJRkVzMlJ6aVQ4Z21IeVFUCm5nbGEvTmJNd1c2L1JNdDN2ME5ubWpGcUV2VzhpSlhNTDRNRjM2dW93VEVLMXQzNU5VTVRZSGh3VG0xVzNqcGMKZUV5R01iSkVPMDJIMlY3K0FuaDVwRGxuWUN0cVNaWjYzL0FrQ01JMWlPNXR3b2ZEWUo5dlQwZGRVRWoyVjhZcgpGYWx6VkdWZllQMUNnWUVBdnNBUkM0dSt3UGhHR0ozSGZGbUhPZTI2ZnpGa3RtSFJBWVJBSU1mVlVrQkFxS01YCjJVVWd2OXFyTVRZbW8xNUVIV3lUTlhGK1dvRmZwazNqUGpOOStkTTBnNXp1UUgvUVZrUGpITHY3TVU3ZnJTcXcKaEZiU3RJRWlpZlFXWTVoRGY0WGdsTlZSWWRqNnE2MEtaYlBxNnNYRXAvaW9LUVZ4cjdFR2JKM0NqdzBDZ1lFQQpmN2Z6dEJTRjlkcXNqdXpoTWxQVXJaMVVOUGs4TnF1dlNGVVBVYXVHY1g1MEF4b3dKYUZxSlkyWmYrVkJKNnRlClZpYmRSODN5TVE5ZzJqZlZJMVdvdVNtL1JGUGhZdWx3dGRCeFl4Y08rUXowcnFjQWluaXJpY3hOYjdBbEJBOVQKVW1YeFpPNVBqcE8ycEUxdXk4RFRFZlFSN2JmWjdOUVpqK3JOZTRGc0RBa0NnWUJaUXh4dlpWdHdEYW80Y1NJcwpZWG0wZVZ2ZmpqZlVFNUdwUG1ZV2piZUJOWHVEdGdpSEhMbDh0YzVYdDZnN3ByNTVQcVpBM2VNaDZMUFZrUmFnCmR6NWZ5MHRJRHVHSXpJdGNsRVhpM2IyYmNqbUhHaDNOeDYvRm5ndXJjVkJLVWNHWFNEVW8vQzVBYm5qcXYvOUQKTHdrV2ZZSTUxQVY5THJHT0FTQVJnY01uNmc9PQotLS0tLUVORCBQUklWQVRFIEtFWS0tLS0t";
    private final String testJwtPublicKey = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF3TkdtNS9wWVV5akRPV2FqNFJUcQpYaXVCTUw5NFNKMWxEM2FETmx4bURVRU80K2xCNUxjUk9rV1pzYW0rOWVkSXR2cUN4TEF4UTZvdFRVMkVaVm4vCkFhajUwSlk1dHFsRlBiSVRvTmk5REtMaDFiWURhRlRueFZGOG9reDBIU0JJejZlcVJ2OFE5TGwxUDBWQjRPVkYKRnphUkt2YVhDbTZwdFFWTlJEQ25aRzVGUDNtcCtDMkc4Wjk1ZlRneVBkRFY5TERNSzl2MzNINGZQSEdrbGVMUQpCaUY5VWM4bTBpSG9HdE5SQnFOaVpJUnVQVlYzUkMzQUhhVGNFZUVFSXQzMnJKRmlhYVFPNkg0V2hUd2RTbVZMCjJYU1VRcDZtcWJzdUlWc3NkWlpNMC80TWdJc0xXbjdEaDh6bUZ6MjdrSk5BRHB2MXNSNVJpaUs2OEpHdnNYc3EKSVFJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";

    @BeforeEach
    void setUp() throws IOException {
        String authJsonContent = """
            [
              {
                "user": "Test User",
                "description": "Test film submitter",
                "token": "testtoken123456789"
              }
            ]
            """;

        when(resourceLoader.getResource("classpath:auth.json")).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(authJsonContent.getBytes()));

        authenticationService = new AuthenticationService(userRepository, resourceLoader, passwordEncoder, testJwtPrivateKey, testJwtPublicKey);
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

        String token = authenticationService.generateJwtToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @Timeout(5)
    void validateFilmToken_ValidToken_ReturnsTrue() {
        boolean result = authenticationService.validateFilmToken("testtoken123456789");

        assertTrue(result);
    }

    @Test
    @Timeout(5)
    void validateFilmToken_InvalidToken_ReturnsFalse() {
        boolean result = authenticationService.validateFilmToken("invalidtoken");

        assertFalse(result);
    }

    @Test
    @Timeout(5)
    void validateFilmToken_NullToken_ReturnsFalse() {
        boolean result = authenticationService.validateFilmToken(null);

        assertFalse(result);
    }
}