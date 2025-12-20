package com.example.voicechanger.controller;

import com.example.voicechanger.dto.UserDetailsRequest;
import com.example.voicechanger.dto.UserDetailsResponse;
import com.example.voicechanger.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-details")
@RequiredArgsConstructor
public class UserDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsController.class);
    private final UserDetailsService userDetailsService;

    /**
     * Create user details for logged-in user
     */
    @PostMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailsResponse> createMyUserDetails(@RequestBody UserDetailsRequest request) {
        logger.info("Creating user details for logged-in user");
        UserDetailsResponse response = userDetailsService.createUserDetails(request);
        logger.info("User details created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get user details for logged-in user
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailsResponse> getMyUserDetails() {
        logger.info("Fetching user details for logged-in user");
        UserDetailsResponse response = userDetailsService.getMyUserDetails();
        return ResponseEntity.ok(response);
    }

    /**
     * Update user details for logged-in user
     */
    @PutMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailsResponse> updateMyUserDetails(@RequestBody UserDetailsRequest request) {
        logger.info("Updating user details for logged-in user");
        UserDetailsResponse response = userDetailsService.updateMyUserDetails(request);
        logger.info("User details updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete user details for logged-in user
     */
    @DeleteMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMyUserDetails() {
        logger.info("Deleting user details for logged-in user");
        userDetailsService.deleteMyUserDetails();
        logger.info("User details deleted successfully");
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user details by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailsResponse> getUserDetailsById(@PathVariable Long id) {
        logger.info("Fetching user details for ID: {}", id);
        UserDetailsResponse response = userDetailsService.getUserDetailsById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all user details (Admin only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDetailsResponse>> getAllUserDetails() {
        logger.info("Fetching all user details");
        List<UserDetailsResponse> response = userDetailsService.getAllUserDetails();
        return ResponseEntity.ok(response);
    }

    /**
     * Update user details by ID (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailsResponse> updateUserDetailsById(
            @PathVariable Long id,
            @RequestBody UserDetailsRequest request) {
        logger.info("Updating user details for ID: {}", id);
        UserDetailsResponse response = userDetailsService.updateUserDetailsById(id, request);
        logger.info("User details updated successfully for ID: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete user details by ID (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserDetailsById(@PathVariable Long id) {
        logger.info("Deleting user details for ID: {}", id);
        userDetailsService.deleteUserDetailsById(id);
        logger.info("User details deleted successfully for ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
