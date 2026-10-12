package com.jfs.cpts.controller;

import com.jfs.cpts.dto.auth.AuthResponse;
import com.jfs.cpts.dto.auth.LoginRequest;
import com.jfs.cpts.dto.auth.RegisterRequest;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.mapper.EntityMapper;
import com.jfs.cpts.security.JwtTokenProvider;
import com.jfs.cpts.security.RateLimiterService;
import com.jfs.cpts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, and profile check endpoints")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RateLimiterService rateLimiterService;
    private final EntityMapper mapper;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          RateLimiterService rateLimiterService,
                          EntityMapper mapper) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.rateLimiterService = rateLimiterService;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        AuthResponse response = new AuthResponse(token, mapper.toUserInfo(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        rateLimiterService.checkLoginRateLimit(request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase().trim(), request.password())
        );

        User user = userService.findByEmail(authentication.getName());
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        AuthResponse response = new AuthResponse(token, mapper.toUserInfo(user));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user details")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new com.jfs.cpts.exception.AppException("Unauthorized: authentication required", HttpStatus.UNAUTHORIZED);
        }
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(mapper.toUserInfo(user));
    }
}
