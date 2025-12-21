package com.example.voicechanger.service;

import com.example.voicechanger.dto.UpdateUserRequest;
import com.example.voicechanger.dto.UserResponse;
import com.example.voicechanger.entity.Role;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.exception.InvalidRequestException;
import com.example.voicechanger.repository.RoleRepository;
import com.example.voicechanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FreeSwitchConfigService freeSwitchConfigService;

    /**
     * Get current logged-in user
     */
    public UserResponse getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        return mapToResponse(user);
    }

    /**
     * Update current logged-in user
     */
    @Transactional
    public UserResponse updateMyProfile(UpdateUserRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // Update basic fields
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        // Note: Regular users cannot change their own roles
        // Role changes should only be done by admins via admin endpoints

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for: {}", username);

        return mapToResponse(updatedUser);
    }

    /**
     * Get all users (Admin only)
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID (Admin only)
     */
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User not found with ID: " + id));

        return mapToResponse(user);
    }

    /**
     * Get user by username (Admin only)
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found with username: " + username));

        return mapToResponse(user);
    }

    /**
     * Update user by ID (Admin only)
     */
    @Transactional
    public UserResponse updateUserById(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User not found with ID: " + id));

        // Update basic fields
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        // Update roles (Admin can change roles)
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new InvalidRequestException("Role not found: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully by admin. ID: {}, Username: {}", id, user.getUsername());

        return mapToResponse(updatedUser);
    }

    /**
     * Delete user by ID (Admin only)
     */
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User not found with ID: " + id));

        // Delete FreeSWITCH configuration
        try {
            freeSwitchConfigService.deleteUserConfig(user.getUsername());
            log.info("FreeSWITCH configuration deleted for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error deleting FreeSWITCH config for user: {}", user.getUsername(), e);
            // Continue with user deletion even if FreeSWITCH config deletion fails
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully. ID: {}, Username: {}", id, user.getUsername());
    }

    /**
     * Enable/Disable user by ID (Admin only)
     */
    @Transactional
    public UserResponse toggleUserStatus(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User not found with ID: " + id));

        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);

        log.info("User status updated. ID: {}, Username: {}, Enabled: {}", id, user.getUsername(), enabled);

        return mapToResponse(updatedUser);
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
