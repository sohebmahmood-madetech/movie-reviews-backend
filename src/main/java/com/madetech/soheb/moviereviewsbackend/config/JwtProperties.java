package com.madetech.soheb.moviereviewsbackend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "moviereviews.auth.review")
public class JwtProperties {
    
    private String privatekey;
    private String publickey;
    
    public String getPrivatekey() {
        return privatekey;
    }
    
    public void setPrivatekey(String privatekey) {
        this.privatekey = privatekey;
    }
    
    public String getPublickey() {
        return publickey;
    }
    
    public void setPublickey(String publickey) {
        this.publickey = publickey;
    }
    
    @PostConstruct
    public void validateKeys() {
        if (!StringUtils.hasText(privatekey)) {
            throw new IllegalStateException("JWT private key is required but not provided. Please set MOVIEREVIEWS_AUTH_REVIEW_PRIVATE_KEY environment variable.");
        }
        
        if (!StringUtils.hasText(publickey)) {
            throw new IllegalStateException("JWT public key is required but not provided. Please set MOVIEREVIEWS_AUTH_REVIEW_PUBLIC_KEY environment variable.");
        }
        
        // Validate that keys are not the hardcoded defaults
        if (privatekey.contains("LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0t")) {
            throw new IllegalStateException("JWT private key appears to be using default hardcoded value. Please provide a secure private key via MOVIEREVIEWS_AUTH_REVIEW_PRIVATE_KEY environment variable.");
        }
        
        if (publickey.contains("LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0")) {
            throw new IllegalStateException("JWT public key appears to be using default hardcoded value. Please provide a secure public key via MOVIEREVIEWS_AUTH_REVIEW_PUBLIC_KEY environment variable.");
        }
    }
}