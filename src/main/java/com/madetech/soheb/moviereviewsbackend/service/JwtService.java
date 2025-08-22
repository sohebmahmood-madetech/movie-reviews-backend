package com.madetech.soheb.moviereviewsbackend.service;

import com.madetech.soheb.moviereviewsbackend.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final int JWT_EXPIRY_DAYS = 30;
    
    private final JwtProperties jwtProperties;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    
    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        try {
            this.privateKey = loadPrivateKey();
            this.publicKey = loadPublicKey();
        } catch (Exception e) {
            logger.error("Failed to load JWT keys", e);
            throw new IllegalStateException("Failed to load JWT keys: " + e.getMessage(), e);
        }
    }
    
    public String generateToken(UUID userId) {
        try {
            Date issuedAt = new Date();
            Date expiry = Date.from(LocalDateTime.now()
                .plusDays(JWT_EXPIRY_DAYS)
                .atZone(ZoneId.systemDefault())
                .toInstant());
            
            return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiry)
                .signWith(privateKey, SignatureAlgorithm.RS512)
                .compact();
        } catch (Exception e) {
            logger.error("Failed to generate JWT token for user: {}", userId, e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
    
    public UUID extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            return UUID.fromString(claims.getSubject());
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token expired for token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new RuntimeException("JWT token expired", e);
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        } catch (Exception e) {
            logger.error("Failed to extract user ID from JWT token", e);
            throw new RuntimeException("Failed to extract user ID from JWT token", e);
        }
    }
    
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error validating JWT token", e);
            return false;
        }
    }
    
    private PrivateKey loadPrivateKey() throws Exception {
        String privateKeyContent = jwtProperties.getPrivatekey();
        
        // Remove PEM headers and whitespace
        privateKeyContent = privateKeyContent
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        return keyFactory.generatePrivate(keySpec);
    }
    
    private PublicKey loadPublicKey() throws Exception {
        String publicKeyContent = jwtProperties.getPublickey();
        
        // Remove PEM headers and whitespace
        publicKeyContent = publicKeyContent
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        return keyFactory.generatePublic(keySpec);
    }
}