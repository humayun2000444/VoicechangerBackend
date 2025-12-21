package com.example.voicechanger.service;

import com.example.voicechanger.dto.UserDetailsRequest;
import com.example.voicechanger.dto.UserDetailsResponse;
import com.example.voicechanger.dto.UserResponse;
import com.example.voicechanger.entity.Role;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.entity.UserDetails;
import com.example.voicechanger.exception.InvalidRequestException;
import com.example.voicechanger.entity.VoiceUserMapping;
import com.example.voicechanger.repository.UserDetailsRepository;
import com.example.voicechanger.repository.UserRepository;
import com.example.voicechanger.repository.VoiceUserMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private final UserDetailsRepository userDetailsRepository;
    private final UserRepository userRepository;
    private final VoiceUserMappingRepository voiceUserMappingRepository;

    /**
     * Create user details for logged-in user
     */
    @Transactional
    public UserDetailsResponse createUserDetails(UserDetailsRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (userDetailsRepository.existsByIdUser(user.getId())) {
            throw new InvalidRequestException("User details already exist. Use update instead.");
        }

        UserDetails userDetails = new UserDetails();
        userDetails.setIdUser(user.getId());
        mapRequestToEntity(request, userDetails);

        UserDetails savedDetails = userDetailsRepository.save(userDetails);

        log.info("User details created for user: {}", username);
        return mapEntityToResponse(savedDetails);
    }

    /**
     * Get user details for logged-in user
     */
    public UserDetailsResponse getMyUserDetails() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        UserDetails userDetails = userDetailsRepository.findByIdUser(user.getId())
                .orElseThrow(() -> new InvalidRequestException("User details not found"));

        return mapEntityToResponse(userDetails);
    }

    /**
     * Get user details by ID
     */
    public UserDetailsResponse getUserDetailsById(Long id) {
        UserDetails userDetails = userDetailsRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User details not found with ID: " + id));

        return mapEntityToResponse(userDetails);
    }

    /**
     * Get all user details
     */
    public List<UserDetailsResponse> getAllUserDetails() {
        return userDetailsRepository.findAll().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update user details for logged-in user
     */
    @Transactional
    public UserDetailsResponse updateMyUserDetails(UserDetailsRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        UserDetails userDetails = userDetailsRepository.findByIdUser(user.getId())
                .orElseThrow(() -> new InvalidRequestException("User details not found. Create details first."));

        mapRequestToEntity(request, userDetails);
        UserDetails updatedDetails = userDetailsRepository.save(userDetails);

        log.info("User details updated for user: {}", username);
        return mapEntityToResponse(updatedDetails);
    }

    /**
     * Update user details by ID (admin operation)
     */
    @Transactional
    public UserDetailsResponse updateUserDetailsById(Long id, UserDetailsRequest request) {
        UserDetails userDetails = userDetailsRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("User details not found with ID: " + id));

        mapRequestToEntity(request, userDetails);
        UserDetails updatedDetails = userDetailsRepository.save(userDetails);

        log.info("User details updated for ID: {}", id);
        return mapEntityToResponse(updatedDetails);
    }

    /**
     * Delete user details for logged-in user
     */
    @Transactional
    public void deleteMyUserDetails() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        UserDetails userDetails = userDetailsRepository.findByIdUser(user.getId())
                .orElseThrow(() -> new InvalidRequestException("User details not found"));

        userDetailsRepository.delete(userDetails);

        log.info("User details deleted for user: {}", username);
    }

    /**
     * Delete user details by ID (admin operation)
     */
    @Transactional
    public void deleteUserDetailsById(Long id) {
        if (!userDetailsRepository.existsById(id)) {
            throw new InvalidRequestException("User details not found with ID: " + id);
        }

        userDetailsRepository.deleteById(id);
        log.info("User details deleted for ID: {}", id);
    }

    /**
     * Set selected default voice type for the logged-in user
     */
    @Transactional
    public UserDetailsResponse setSelectedVoiceType(Long voiceTypeId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // Verify the user has active access to this voice type
        List<VoiceUserMapping> activeVoices = voiceUserMappingRepository.findActiveByIdUser(user.getId(), LocalDateTime.now());
        boolean hasAccess = activeVoices.stream()
                .anyMatch(m -> m.getIdVoiceType().equals(voiceTypeId));

        if (!hasAccess) {
            throw new InvalidRequestException("You don't have active access to voice type ID: " + voiceTypeId);
        }

        // Get or create user details
        UserDetails userDetails = userDetailsRepository.findByIdUser(user.getId())
                .orElseGet(() -> {
                    UserDetails newDetails = new UserDetails();
                    newDetails.setIdUser(user.getId());
                    return newDetails;
                });

        // Set selected voice type
        userDetails.setSelectedVoiceTypeId(voiceTypeId);
        UserDetails savedDetails = userDetailsRepository.save(userDetails);

        log.info("Selected voice type set to {} for user: {}", voiceTypeId, username);
        return mapEntityToResponse(savedDetails);
    }

    /**
     * Map request DTO to entity
     */
    private void mapRequestToEntity(UserDetailsRequest request, UserDetails entity) {
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isEmpty()) {
            entity.setDateOfBirth(LocalDate.parse(request.getDateOfBirth(), DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (request.getGender() != null) {
            entity.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            entity.setAddress(request.getAddress());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getProfilePhoto() != null) {
            entity.setProfilePhoto(request.getProfilePhoto());
        }
    }

    /**
     * Map entity to response DTO
     */
    private UserDetailsResponse mapEntityToResponse(UserDetails entity) {
        // Fetch the related user
        User user = userRepository.findById(entity.getIdUser())
                .orElseThrow(() -> new InvalidRequestException("Related user not found for user_details ID: " + entity.getIdUserDetails()));

        // Map user to UserResponse
        UserResponse userResponse = UserResponse.builder()
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

        return UserDetailsResponse.builder()
                .idUserDetails(entity.getIdUserDetails())
                .idUser(entity.getIdUser())
                .user(userResponse)
                .dateOfBirth(entity.getDateOfBirth() != null ? entity.getDateOfBirth().toString() : null)
                .gender(entity.getGender())
                .address(entity.getAddress())
                .email(entity.getEmail())
                .profilePhoto(entity.getProfilePhoto())
                .selectedVoiceTypeId(entity.getSelectedVoiceTypeId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
