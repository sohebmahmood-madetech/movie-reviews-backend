package com.madetech.soheb.moviereviewsbackend.config;

import com.madetech.soheb.moviereviewsbackend.service.JwtService;
import com.madetech.soheb.moviereviewsbackend.service.FilmTokenAuthenticationService;
import com.madetech.soheb.moviereviewsbackend.service.RateLimitService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
    
    @Bean
    @Primary
    public JwtService testJwtService() {
        JwtService mockJwtService = mock(JwtService.class);
        // Set up default behavior for JWT service
        when(mockJwtService.generateToken(any())).thenReturn("test-jwt-token");
        when(mockJwtService.isTokenValid(any())).thenReturn(true);
        return mockJwtService;
    }
    
    @Bean
    @Primary
    public JwtProperties testJwtProperties() {
        JwtProperties mockProperties = mock(JwtProperties.class);
        when(mockProperties.getPrivatekey()).thenReturn("test-private-key");
        when(mockProperties.getPublickey()).thenReturn("test-public-key");
        return mockProperties;
    }
    
    @Bean
    @Primary
    public FilmTokenAuthenticationService testFilmTokenAuthenticationService() {
        FilmTokenAuthenticationService mockService = mock(FilmTokenAuthenticationService.class);
        // Return true for the valid token used in tests
        when(mockService.isValidToken("a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2g3h4i5")).thenReturn(true);
        // Return false for invalid tokens
        when(mockService.isValidToken("invalid-token")).thenReturn(false);
        return mockService;
    }
    
    @Bean
    @Primary
    public RateLimitService testRateLimitService() {
        RateLimitService mockService = mock(RateLimitService.class);
        when(mockService.isAllowed(any())).thenReturn(true);
        return mockService;
    }
}