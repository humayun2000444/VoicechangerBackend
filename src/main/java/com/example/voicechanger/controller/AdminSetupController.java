package com.example.voicechanger.controller;

import com.example.voicechanger.entity.Role;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.repository.RoleRepository;
import com.example.voicechanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class AdminSetupController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    public ResponseEntity<?> createOrUpdateAdmin(@RequestBody Map<String, String> request) {
        String username = request.getOrDefault("username", "admin");
        String password = request.getOrDefault("password", "admin123");
        String firstName = request.getOrDefault("firstName", "Admin");
        String lastName = request.getOrDefault("lastName", "User");

        // Get or create roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);

        // Check if user exists
        User user = userRepository.findByUsername(username)
                .orElse(new User());

        // Update user details
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Admin user created/updated successfully",
            "username", savedUser.getUsername(),
            "password", password,
            "roles", savedUser.getRoles().stream().map(Role::getName).toList(),
            "passwordHash", savedUser.getPassword()
        ));
    }

    @GetMapping("/test-admin")
    public ResponseEntity<?> testAdmin() {
        User admin = userRepository.findByUsername("admin").orElse(null);

        if (admin == null) {
            return ResponseEntity.ok(Map.of(
                "exists", false,
                "message", "Admin user not found. Use POST /api/setup/create-admin to create it."
            ));
        }

        return ResponseEntity.ok(Map.of(
            "exists", true,
            "username", admin.getUsername(),
            "enabled", admin.isEnabled(),
            "roles", admin.getRoles().stream().map(Role::getName).toList(),
            "passwordHash", admin.getPassword()
        ));
    }
}
