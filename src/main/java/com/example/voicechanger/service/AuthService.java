package com.example.voicechanger.service;

import com.example.voicechanger.dto.AuthResponse;
import com.example.voicechanger.dto.LoginRequest;
import com.example.voicechanger.dto.SignupRequest;
import com.example.voicechanger.entity.Role;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.exception.InvalidRequestException;
import com.example.voicechanger.exception.UsernameAlreadyExistsException;
import com.example.voicechanger.repository.RoleRepository;
import com.example.voicechanger.repository.UserRepository;
import com.example.voicechanger.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final FreeSwitchConfigService freeSwitchConfigService;
    private final BalanceService balanceService;
    private final VoicePurchaseService voicePurchaseService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Validate request
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new InvalidRequestException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new InvalidRequestException("Password is required");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new InvalidRequestException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new InvalidRequestException("Last name is required");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username '" + request.getUsername() + "' is already taken");
        }

        // Get or create ROLE_USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_USER");
                    return roleRepository.save(newRole);
                });

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);

        // Assign ROLE_USER by default
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Save user to database
        User savedUser = userRepository.save(user);

        // Create FreeSWITCH user configuration
        // If this throws an exception, the transaction will be rolled back and user won't be created
        log.info("Creating FreeSWITCH configuration for user: {}", savedUser.getUsername());
        freeSwitchConfigService.createUserConfig(savedUser.getUsername());
        log.info("FreeSWITCH configuration created successfully for user: {}", savedUser.getUsername());

        // Add 30-second welcome balance for new user
        log.info("Adding 30-second welcome balance for user: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        balanceService.addBalance(savedUser.getId(), 30L);
        log.info("Welcome balance added successfully for user: {}", savedUser.getUsername());

        // Assign default voice types (ID 3 and 4) to new user
        log.info("Assigning default voice types to user: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        voicePurchaseService.assignDefaultVoiceTypes(savedUser.getId());
        log.info("Default voice types assigned successfully for user: {}", savedUser.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);

        // Get role names
        List<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Return response
        return new AuthResponse(
                token,
                savedUser.getUsername(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                roleNames,
                savedUser.getCreatedAt(),
                "User registered successfully"
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Validate request
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new InvalidRequestException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new InvalidRequestException("Password is required");
        }

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Get user details
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Get role names
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Return response
        return new AuthResponse(
                token,
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                roleNames,
                user.getCreatedAt(),
                "Login successful"
        );
    }
}
