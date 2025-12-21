package com.example.voicechanger.service;

import com.example.voicechanger.dto.VoicePurchaseRequest;
import com.example.voicechanger.dto.VoicePurchaseResponse;
import com.example.voicechanger.dto.VoiceTypeResponse;
import com.example.voicechanger.dto.VoiceUserMappingResponse;
import com.example.voicechanger.dto.UserResponse;
import com.example.voicechanger.entity.*;
import com.example.voicechanger.exception.InvalidRequestException;
import com.example.voicechanger.repository.VoicePurchaseRepository;
import com.example.voicechanger.repository.VoiceTypeRepository;
import com.example.voicechanger.repository.VoiceUserMappingRepository;
import com.example.voicechanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoicePurchaseService {

    private final VoicePurchaseRepository voicePurchaseRepository;
    private final VoiceUserMappingRepository voiceUserMappingRepository;
    private final VoiceTypeRepository voiceTypeRepository;
    private final UserRepository userRepository;

    // Default price for voice type purchase (can be made configurable)
    private static final BigDecimal VOICE_PURCHASE_PRICE = new BigDecimal("50.00");

    /**
     * Request to purchase a voice type (creates pending purchase request with transaction)
     * Admin approval is required to grant access
     */
    @Transactional
    public VoicePurchaseResponse purchaseVoiceType(VoicePurchaseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User {} requesting to purchase voice type ID: {} with payment method: {}, tnxId: {}, subscription: {}",
                username, request.getIdVoiceType(), request.getTransactionMethod(), request.getTnxId(), request.getSubscriptionType());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found: " + username));

        // Verify voice type exists
        VoiceType voiceType = voiceTypeRepository.findById(request.getIdVoiceType())
                .orElseThrow(() -> new InvalidRequestException("Voice type not found with ID: " + request.getIdVoiceType()));

        // Check if user already has an approved purchase for this voice type
        if (voicePurchaseRepository.existsByIdUserAndIdVoiceTypeAndStatus(user.getId(), request.getIdVoiceType(), "approved")) {
            throw new InvalidRequestException("You have already purchased this voice type");
        }

        // Check if user already has a pending purchase request for this voice type
        if (voicePurchaseRepository.existsByIdUserAndIdVoiceTypeAndStatus(user.getId(), request.getIdVoiceType(), "pending")) {
            throw new InvalidRequestException("You already have a pending purchase request for this voice type");
        }

        // Check if user already has active (non-expired) purchased access to this voice type
        List<VoiceUserMapping> activeVoices = voiceUserMappingRepository.findActiveByIdUser(user.getId(), LocalDateTime.now());
        boolean hasActiveAccess = activeVoices.stream()
                .anyMatch(m -> m.getIdVoiceType().equals(request.getIdVoiceType()) && m.getIsPurchased());

        if (hasActiveAccess) {
            throw new InvalidRequestException("You already have active access to this voice type: " + voiceType.getVoiceName());
        }

        // Validate subscription type
        String subscriptionType = request.getSubscriptionType().toLowerCase();
        if (!subscriptionType.equals("monthly") && !subscriptionType.equals("yearly")) {
            throw new InvalidRequestException("Invalid subscription type. Must be 'monthly' or 'yearly'");
        }

        // Calculate expiry date based on subscription type
        LocalDateTime expiryDate = LocalDateTime.now();
        if (subscriptionType.equals("monthly")) {
            expiryDate = expiryDate.plusMonths(1);
        } else { // yearly
            expiryDate = expiryDate.plusYears(1);
        }

        // Create pending purchase request with payment info stored directly
        VoicePurchase purchase = VoicePurchase.builder()
                .idUser(user.getId())
                .idVoiceType(request.getIdVoiceType())
                .transactionMethod(request.getTransactionMethod())
                .tnxId(request.getTnxId())
                .subscriptionType(subscriptionType)
                .expiryDate(expiryDate)
                .amount(VOICE_PURCHASE_PRICE)
                .status("pending") // Waiting for admin approval
                .build();

        VoicePurchase savedPurchase = voicePurchaseRepository.save(purchase);

        log.info("Voice type purchase request created (pending approval) for user {} and voice type {} with tnxId: {}, subscription: {}, expires: {}",
                username, voiceType.getVoiceName(), request.getTnxId(), subscriptionType, expiryDate);

        return mapPurchaseToResponse(savedPurchase);
    }

    /**
     * Get all voice purchases for the authenticated user
     */
    @Transactional(readOnly = true)
    public List<VoicePurchaseResponse> getMyPurchases() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching voice purchases for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found: " + username));

        return voicePurchaseRepository.findByIdUserOrderByPurchaseDateDesc(user.getId()).stream()
                .map(this::mapPurchaseToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all voice purchases (Admin only)
     */
    @Transactional(readOnly = true)
    public List<VoicePurchaseResponse> getAllPurchases() {
        log.info("Fetching all voice purchases");
        return voicePurchaseRepository.findAll().stream()
                .map(this::mapPurchaseToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get purchases for a specific voice type (Admin only)
     */
    @Transactional(readOnly = true)
    public List<VoicePurchaseResponse> getPurchasesByVoiceType(Long voiceTypeId) {
        log.info("Fetching purchases for voice type ID: {}", voiceTypeId);
        return voicePurchaseRepository.findByIdVoiceType(voiceTypeId).stream()
                .map(this::mapPurchaseToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all pending voice purchase requests (Admin only)
     */
    @Transactional(readOnly = true)
    public List<VoicePurchaseResponse> getPendingPurchases() {
        log.info("Fetching all pending voice purchase requests");
        return voicePurchaseRepository.findByStatus("pending").stream()
                .map(this::mapPurchaseToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approve a voice purchase request (Admin only)
     * This grants the user access to the voice type and updates transaction status
     */
    @Transactional
    public VoicePurchaseResponse approvePurchase(Long purchaseId) {
        log.info("Admin approving voice purchase request ID: {}", purchaseId);

        VoicePurchase purchase = voicePurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new InvalidRequestException("Purchase request not found with ID: " + purchaseId));

        if (!"pending".equals(purchase.getStatus())) {
            throw new InvalidRequestException("Only pending purchase requests can be approved. Current status: " + purchase.getStatus());
        }

        // Update purchase status to approved
        purchase.setStatus("approved");
        purchase.setUpdatedAt(LocalDateTime.now());
        VoicePurchase savedPurchase = voicePurchaseRepository.save(purchase);

        // Grant user access to the voice type
        voiceUserMappingRepository.findByIdUserAndIdVoiceType(purchase.getIdUser(), purchase.getIdVoiceType())
                .ifPresentOrElse(
                        existingMapping -> {
                            // Update existing trial or free access to purchased
                            existingMapping.setIsPurchased(true);
                            existingMapping.setTrialExpiryDate(null); // Remove trial expiry
                            existingMapping.setExpiryDate(purchase.getExpiryDate()); // Set subscription expiry
                            voiceUserMappingRepository.save(existingMapping);
                            log.info("Updated existing mapping to purchased for user {} and voice type {} with expiry: {}",
                                    purchase.getIdUser(), purchase.getIdVoiceType(), purchase.getExpiryDate());
                        },
                        () -> {
                            // Create new voice-user mapping with purchased access
                            VoiceUserMapping mapping = VoiceUserMapping.builder()
                                    .idUser(purchase.getIdUser())
                                    .idVoiceType(purchase.getIdVoiceType())
                                    .isPurchased(true)
                                    .trialExpiryDate(null) // No trial
                                    .expiryDate(purchase.getExpiryDate()) // Set subscription expiry date
                                    .build();
                            voiceUserMappingRepository.save(mapping);
                            log.info("Created new purchased mapping for user {} and voice type {} with expiry: {}",
                                    purchase.getIdUser(), purchase.getIdVoiceType(), purchase.getExpiryDate());
                        }
                );

        log.info("Voice purchase request approved and access granted. Purchase ID: {}", purchaseId);
        return mapPurchaseToResponse(savedPurchase);
    }

    /**
     * Reject a voice purchase request (Admin only)
     * Also updates the associated transaction status
     */
    @Transactional
    public VoicePurchaseResponse rejectPurchase(Long purchaseId) {
        log.info("Admin rejecting voice purchase request ID: {}", purchaseId);

        VoicePurchase purchase = voicePurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new InvalidRequestException("Purchase request not found with ID: " + purchaseId));

        if (!"pending".equals(purchase.getStatus())) {
            throw new InvalidRequestException("Only pending purchase requests can be rejected. Current status: " + purchase.getStatus());
        }

        // Update purchase status to rejected
        purchase.setStatus("rejected");
        purchase.setUpdatedAt(LocalDateTime.now());
        VoicePurchase savedPurchase = voicePurchaseRepository.save(purchase);

        log.info("Voice purchase request rejected. Purchase ID: {}", purchaseId);
        return mapPurchaseToResponse(savedPurchase);
    }

    /**
     * Get all voice types available to the authenticated user (including expired trials)
     */
    @Transactional(readOnly = true)
    public List<VoiceUserMappingResponse> getMyVoiceTypes() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching all voice types for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found: " + username));

        return voiceUserMappingRepository.findByIdUser(user.getId()).stream()
                .map(this::mapMappingToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get only active (non-expired) voice types for the authenticated user
     */
    @Transactional(readOnly = true)
    public List<VoiceUserMappingResponse> getMyActiveVoiceTypes() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching active voice types for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found: " + username));

        return voiceUserMappingRepository.findActiveByIdUser(user.getId(), LocalDateTime.now()).stream()
                .map(this::mapMappingToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get available voice types for purchase (excludes voice types user already has)
     * Only returns voice types that the user has NOT purchased or been assigned
     */
    @Transactional(readOnly = true)
    public List<VoiceTypeResponse> getAllVoiceTypesWithAccess() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found: " + username));

        // Get all voice types the user currently has access to (purchased or assigned)
        List<Long> userVoiceTypeIds = voiceUserMappingRepository.findByIdUser(user.getId())
                .stream()
                .map(VoiceUserMapping::getIdVoiceType)
                .collect(Collectors.toList());

        // Return only voice types that user does NOT already have
        return voiceTypeRepository.findAll().stream()
                .filter(vt -> !userVoiceTypeIds.contains(vt.getId())) // Filter out voice types user already has
                .map(vt -> VoiceTypeResponse.builder()
                        .id(vt.getId())
                        .voiceName(vt.getVoiceName())
                        .code(vt.getCode())
                        .createdAt(vt.getCreatedAt())
                        .updatedAt(vt.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Assign default voice types to a new user (called during registration)
     * Voice type 3 (Child Voice) - 3-day trial
     * Voice type 4 (Robot Voice) - permanent free access
     */
    @Transactional
    public void assignDefaultVoiceTypes(Long userId) {
        log.info("Assigning default voice types (3 and 4) to user ID: {}", userId);

        // Assign voice type ID 3 (Child Voice) - 3-day trial
        if (!voiceUserMappingRepository.existsByIdUserAndIdVoiceType(userId, 3L)) {
            LocalDateTime trialExpiry = LocalDateTime.now().plusDays(3);
            VoiceUserMapping mapping3 = VoiceUserMapping.builder()
                    .idUser(userId)
                    .idVoiceType(3L)
                    .isPurchased(false) // Free trial
                    .trialExpiryDate(trialExpiry) // Expires in 3 days
                    .build();
            voiceUserMappingRepository.save(mapping3);
            log.info("Voice type ID 3 (Child Voice) assigned to user ID: {} with 3-day trial expiring at: {}", userId, trialExpiry);
        }

        // Assign voice type ID 4 (Robot Voice) - permanent free access
        if (!voiceUserMappingRepository.existsByIdUserAndIdVoiceType(userId, 4L)) {
            VoiceUserMapping mapping4 = VoiceUserMapping.builder()
                    .idUser(userId)
                    .idVoiceType(4L)
                    .isPurchased(false) // Free, auto-assigned
                    .trialExpiryDate(null) // No expiry - permanent access
                    .build();
            voiceUserMappingRepository.save(mapping4);
            log.info("Voice type ID 4 (Robot Voice) assigned to user ID: {} with permanent access", userId);
        }

        log.info("Default voice types successfully assigned to user ID: {}", userId);
    }

    /**
     * Delete a voice purchase (Admin only)
     */
    @Transactional
    public void deletePurchase(Long purchaseId) {
        log.info("Deleting voice purchase ID: {}", purchaseId);

        VoicePurchase purchase = voicePurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new InvalidRequestException("Purchase not found with ID: " + purchaseId));

        // Also remove the voice-user mapping if it was purchased
        voiceUserMappingRepository.findByIdUserAndIdVoiceType(purchase.getIdUser(), purchase.getIdVoiceType())
                .ifPresent(mapping -> {
                    if (mapping.getIsPurchased()) {
                        voiceUserMappingRepository.delete(mapping);
                        log.info("Removed voice-user mapping for user {} and voice type {}",
                                purchase.getIdUser(), purchase.getIdVoiceType());
                    }
                });

        voicePurchaseRepository.delete(purchase);
        log.info("Voice purchase deleted successfully. ID: {}", purchaseId);
    }

    private VoicePurchaseResponse mapPurchaseToResponse(VoicePurchase purchase) {
        VoicePurchaseResponse.VoicePurchaseResponseBuilder builder = VoicePurchaseResponse.builder()
                .id(purchase.getId())
                .idUser(purchase.getIdUser())
                .idVoiceType(purchase.getIdVoiceType())
                .idTransaction(purchase.getIdTransaction())
                .transactionMethod(purchase.getTransactionMethod())
                .tnxId(purchase.getTnxId())
                .subscriptionType(purchase.getSubscriptionType())
                .amount(purchase.getAmount())
                .purchaseDate(purchase.getPurchaseDate())
                .expiryDate(purchase.getExpiryDate())
                .status(purchase.getStatus())
                .updatedAt(purchase.getUpdatedAt());

        // Include voice type details
        voiceTypeRepository.findById(purchase.getIdVoiceType()).ifPresent(vt -> {
            VoiceTypeResponse vtResponse = VoiceTypeResponse.builder()
                    .id(vt.getId())
                    .voiceName(vt.getVoiceName())
                    .code(vt.getCode())
                    .createdAt(vt.getCreatedAt())
                    .updatedAt(vt.getUpdatedAt())
                    .build();
            builder.voiceType(vtResponse);
        });

        // Include user details
        userRepository.findById(purchase.getIdUser()).ifPresent(user -> {
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
            builder.user(userResponse);
        });

        return builder.build();
    }

    private VoiceUserMappingResponse mapMappingToResponse(VoiceUserMapping mapping) {
        VoiceUserMappingResponse.VoiceUserMappingResponseBuilder builder = VoiceUserMappingResponse.builder()
                .id(mapping.getId())
                .idUser(mapping.getIdUser())
                .idVoiceType(mapping.getIdVoiceType())
                .isPurchased(mapping.getIsPurchased())
                .assignedAt(mapping.getAssignedAt())
                .trialExpiryDate(mapping.getTrialExpiryDate())
                .expiryDate(mapping.getExpiryDate());

        // Include voice type details
        voiceTypeRepository.findById(mapping.getIdVoiceType()).ifPresent(vt -> {
            VoiceTypeResponse vtResponse = VoiceTypeResponse.builder()
                    .id(vt.getId())
                    .voiceName(vt.getVoiceName())
                    .code(vt.getCode())
                    .createdAt(vt.getCreatedAt())
                    .updatedAt(vt.getUpdatedAt())
                    .build();
            builder.voiceType(vtResponse);
        });

        return builder.build();
    }
}
