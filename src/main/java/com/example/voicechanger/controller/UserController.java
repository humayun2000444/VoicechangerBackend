package com.example.voicechanger.controller;

import com.example.voicechanger.dto.UpdateUserRequest;
import com.example.voicechanger.dto.UserResponse;
import com.example.voicechanger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    /**
     * Get current logged-in user profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyProfile() {
        logger.info("Fetching profile for logged-in user");
        UserResponse response = userService.getMyProfile();
        return ResponseEntity.ok(response);
    }

    /**
     * Update current logged-in user profile
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UpdateUserRequest request) {
        logger.info("Updating profile for logged-in user");
        UserResponse response = userService.updateMyProfile(request);
        logger.info("User profile updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get all users (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        logger.info("Fetching all users");
        List<UserResponse> users = userService.getAllUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Users retrieved successfully");
        response.put("data", users);

        return ResponseEntity.ok(response);
    }

    /**
     * Get user by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("Fetching user by ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user by username (Admin only)
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        logger.info("Fetching user by username: {}", username);
        UserResponse response = userService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user by ID (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserById(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        logger.info("Updating user by ID: {}", id);
        UserResponse response = userService.updateUserById(id, request);
        logger.info("User updated successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete user by ID (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        logger.info("Deleting user by ID: {}", id);
        userService.deleteUserById(id);
        logger.info("User deleted successfully for ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Enable user by ID (Admin only)
     */
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> enableUser(@PathVariable Long id) {
        logger.info("Enabling user ID: {}", id);
        UserResponse user = userService.toggleUserStatus(id, true);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User enabled successfully");
        response.put("data", user);

        logger.info("User enabled successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Disable user by ID (Admin only)
     */
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> disableUser(@PathVariable Long id) {
        logger.info("Disabling user ID: {}", id);
        UserResponse user = userService.toggleUserStatus(id, false);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User disabled successfully");
        response.put("data", user);

        logger.info("User disabled successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }
}
