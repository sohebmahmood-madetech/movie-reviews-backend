package com.madetech.soheb.moviereviewsbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    @Value("${moviereviews.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    @Value("${moviereviews.security.rate-limit.burst-capacity:10}")
    private int burstCapacity;
    
    private final ConcurrentMap<String, ClientRateLimit> rateLimitMap = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String clientId) {
        ClientRateLimit clientLimit = rateLimitMap.computeIfAbsent(clientId, k -> new ClientRateLimit());
        
        synchronized (clientLimit) {
            LocalDateTime now = LocalDateTime.now();
            
            // Clean up old entries periodically
            if (rateLimitMap.size() > 10000) {
                cleanupOldEntries();
            }
            
            // Reset if a minute has passed
            if (ChronoUnit.MINUTES.between(clientLimit.windowStart, now) >= 1) {
                clientLimit.windowStart = now;
                clientLimit.requestCount = 0;
            }
            
            // Check burst capacity
            if (clientLimit.requestCount >= burstCapacity && 
                ChronoUnit.SECONDS.between(clientLimit.lastRequest, now) < 1) {
                logger.debug("Rate limit exceeded for client: {} (burst)", clientId);
                return false;
            }
            
            // Check requests per minute
            if (clientLimit.requestCount >= requestsPerMinute) {
                logger.debug("Rate limit exceeded for client: {} (per minute)", clientId);
                return false;
            }
            
            clientLimit.requestCount++;
            clientLimit.lastRequest = now;
            return true;
        }
    }
    
    private void cleanupOldEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(2);
        rateLimitMap.entrySet().removeIf(entry -> 
            entry.getValue().windowStart.isBefore(cutoff));
    }
    
    private static class ClientRateLimit {
        private LocalDateTime windowStart = LocalDateTime.now();
        private LocalDateTime lastRequest = LocalDateTime.now();
        private int requestCount = 0;
    }
}