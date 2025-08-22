package com.madetech.soheb.moviereviewsbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmTokenAuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FilmTokenAuthenticationService.class);
    private static final String AUTH_FILE_PATH = "auth.json";
    
    private final ObjectMapper objectMapper;
    private final TokenGenerationService tokenGenerationService;
    private Set<String> validTokens;
    
    public FilmTokenAuthenticationService(ObjectMapper objectMapper, TokenGenerationService tokenGenerationService) {
        this.objectMapper = objectMapper;
        this.tokenGenerationService = tokenGenerationService;
    }
    
    @PostConstruct
    public void loadTokens() {
        try {
            ClassPathResource resource = new ClassPathResource(AUTH_FILE_PATH);
            if (!resource.exists()) {
                logger.warn("Auth file not found: {}. Film token authentication will not work.", AUTH_FILE_PATH);
                this.validTokens = Set.of();
                return;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                List<Map<String, String>> authData = objectMapper.readValue(
                    content, 
                    new TypeReference<List<Map<String, String>>>() {}
                );
                
                this.validTokens = authData.stream()
                    .map(entry -> entry.get("token"))
                    .filter(StringUtils::hasText)
                    .filter(token -> {
                        boolean isValid = tokenGenerationService.isValidTokenFormat(token);
                        if (!isValid) {
                            logger.warn("Invalid token format found in auth.json - token will be ignored for security");
                        }
                        return isValid;
                    })
                    .collect(Collectors.toSet());
                
                logger.info("Loaded {} valid film submission tokens from {}", validTokens.size(), AUTH_FILE_PATH);
            }
        } catch (IOException e) {
            logger.error("Failed to load authentication tokens from {}", AUTH_FILE_PATH, e);
            throw new IllegalStateException("Failed to load film authentication tokens", e);
        }
    }
    
    public boolean isValidToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        
        boolean isValid = validTokens.contains(token.trim());
        if (!isValid) {
            logger.debug("Invalid film submission token attempted");
        }
        
        return isValid;
    }
    
    public int getValidTokenCount() {
        return validTokens.size();
    }
}