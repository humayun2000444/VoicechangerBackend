package com.example.voicechanger.controller;

import com.example.voicechanger.dto.AuthResponse;
import com.example.voicechanger.dto.LoginRequest;
import com.example.voicechanger.dto.SignupRequest;
import com.example.voicechanger.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        logger.info("Signup request received for username: {}", request.getUsername());
        AuthResponse response = authService.signup(request);
        logger.info("User registered successfully: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        logger.info("Login request received for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        logger.info("User logged in successfully: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }
}
