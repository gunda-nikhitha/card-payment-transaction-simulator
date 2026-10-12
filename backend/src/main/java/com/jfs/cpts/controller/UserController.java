package com.jfs.cpts.controller;

import com.jfs.cpts.dto.auth.AuthResponse;
import com.jfs.cpts.dto.user.ChangePasswordRequest;
import com.jfs.cpts.dto.user.UpdateProfileRequest;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.mapper.EntityMapper;
import com.jfs.cpts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "User profile retrieval, updates, and password management")
public class UserController {

    private final UserService userService;
    private final EntityMapper mapper;

    public UserController(UserService userService, EntityMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile details")
    public ResponseEntity<AuthResponse.UserInfo> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(mapper.toUserInfo(user));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user name/profile details")
    public ResponseEntity<AuthResponse.UserInfo> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        User updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(mapper.toUserInfo(updated));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change account password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
