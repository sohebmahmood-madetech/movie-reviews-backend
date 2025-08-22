package com.madetech.soheb.moviereviewsbackend.config;

import com.madetech.soheb.moviereviewsbackend.service.FilmTokenAuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class FilmTokenAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(FilmTokenAuthenticationFilter.class);
    private static final String HEADER_NAME = "X-API-AUTH";
    private static final String ROLE_FILM_SUBMITTER = "ROLE_FILM_SUBMITTER";
    
    private final FilmTokenAuthenticationService filmTokenAuthenticationService;
    
    public FilmTokenAuthenticationFilter(FilmTokenAuthenticationService filmTokenAuthenticationService) {
        this.filmTokenAuthenticationService = filmTokenAuthenticationService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (filmTokenAuthenticationService.isValidToken(token)) {
                // Create authentication with FILM_SUBMITTER role
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "film-submitter",
                    null,
                    List.of(new SimpleGrantedAuthority(ROLE_FILM_SUBMITTER))
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Successfully authenticated film submitter with token");
            } else {
                logger.debug("Invalid film submission token attempted");
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_NAME);
        if (headerValue != null && !headerValue.trim().isEmpty()) {
            return headerValue.trim();
        }
        return null;
    }
}