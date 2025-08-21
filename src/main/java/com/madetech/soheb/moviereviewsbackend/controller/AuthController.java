package com.madetech.soheb.moviereviewsbackend.controller;

import com.madetech.soheb.moviereviewsbackend.data.*;
import com.madetech.soheb.moviereviewsbackend.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody UserRegistrationRequest request, 
                                                      BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.failure(new ApiError(1001L, "Invalid registration data provided"))
                );
            }

            Optional<User> userOpt = authenticationService.registerUser(request);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.failure(new ApiError(1002L, "Username or email already exists"))
                );
            }

            String jwtToken = authenticationService.generateJwtToken(userOpt.get());
            return ResponseEntity.ok(ApiResponse.success(jwtToken));
            
        } catch (RuntimeException e) {
            log.error("Signup failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.failure(new ApiError(1003L, "Registration process encountered an error"))
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody UserLoginRequest request, 
                                                     BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.failure(new ApiError(1004L, "Invalid login data provided"))
                );
            }

            Optional<User> userOpt = authenticationService.authenticateUser(request);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.failure(new ApiError(1005L, "Invalid credentials or account access restricted"))
                );
            }

            String jwtToken = authenticationService.generateJwtToken(userOpt.get());
            return ResponseEntity.ok(ApiResponse.success(jwtToken));
            
        } catch (RuntimeException e) {
            log.error("Login failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.failure(new ApiError(1006L, "Authentication process encountered an error"))
            );
        }
    }
}