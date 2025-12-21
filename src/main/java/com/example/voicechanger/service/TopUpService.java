package com.example.voicechanger.service;

import com.example.voicechanger.dto.TopUpRequest;
import com.example.voicechanger.dto.TopUpResponse;
import com.example.voicechanger.dto.UserResponse;
import com.example.voicechanger.entity.Role;
import com.example.voicechanger.entity.Transaction;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.exception.InvalidRequestException;
import com.example.voicechanger.repository.TransactionRepository;
import com.example.voicechanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopUpService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;

    // Rate: 60 seconds for 3 BDT
    private static final BigDecimal RATE_BDT = new BigDecimal("3");
    private static final long RATE_SECONDS = 60L;

    /**
     * Calculate duration in seconds based on amount
     */
    private Long calculateDuration(BigDecimal amount) {
        // duration = (amount / 3) * 60
        BigDecimal multiplier = amount.divide(RATE_BDT, 2, BigDecimal.ROUND_DOWN);
        return multiplier.multiply(new BigDecimal(RATE_SECONDS)).longValue();
    }

    /**
     * Create a new top-up request
     */
    @Transactional
    public TopUpResponse createTopUpRequest(TopUpRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Creating top-up request for user: {} with amount: {} BDT", username, request.getAmount());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found: " + username));

        // Check if transaction ID already exists
        if (transactionRepository.existsByTnxId(request.getTnxId())) {
            throw new InvalidRequestException("Transaction ID already exists: " + request.getTnxId());
        }

        // Create transaction with PENDING status
        Transaction transaction = Transaction.builder()
                .idUser(user.getId())
                .transactionMethod(request.getTransactionMethod())
                .amount(request.getAmount())
                .tnxId(request.getTnxId())
                .status("PENDING")
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Top-up request created successfully. ID: {}, User: {}, Amount: {} BDT, Status: PENDING",
                savedTransaction.getId(), username, request.getAmount());

        return mapToResponse(savedTransaction);
    }

    /**
     * Get authenticated user's top-up requests
     */
    @Transactional(readOnly = true)
    public List<TopUpResponse> getMyTopUpRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching top-up requests for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRequestException("User not found: " + username));

        return transactionRepository.findByIdUser(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all top-up requests (Admin only)
     */
    @Transactional(readOnly = true)
    public List<TopUpResponse> getAllTopUpRequests() {
        log.info("Fetching all top-up requests");
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending top-up requests (Admin only)
     */
    @Transactional(readOnly = true)
    public List<TopUpResponse> getPendingTopUpRequests() {
        log.info("Fetching pending top-up requests");
        return transactionRepository.findByStatus("PENDING").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get top-up request by ID
     */
    @Transactional(readOnly = true)
    public TopUpResponse getTopUpRequestById(Long id) {
        log.info("Fetching top-up request by ID: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("Top-up request not found with ID: " + id));
        return mapToResponse(transaction);
    }

    /**
     * Approve top-up request (Admin only)
     */
    @Transactional
    public TopUpResponse approveTopUpRequest(Long id) {
        log.info("Approving top-up request ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("Top-up request not found with ID: " + id));

        if (!"PENDING".equals(transaction.getStatus())) {
            throw new InvalidRequestException("Only PENDING requests can be approved. Current status: " + transaction.getStatus());
        }

        // Calculate duration based on amount
        Long durationInSeconds = calculateDuration(transaction.getAmount());

        // Update transaction status to SUCCESS
        transaction.setStatus("SUCCESS");
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Add balance to user
        balanceService.addBalance(transaction.getIdUser(), durationInSeconds);

        log.info("Top-up request approved successfully. ID: {}, User ID: {}, Amount: {} BDT, Duration added: {} seconds",
                id, transaction.getIdUser(), transaction.getAmount(), durationInSeconds);

        return mapToResponse(savedTransaction);
    }

    /**
     * Reject top-up request (Admin only)
     */
    @Transactional
    public TopUpResponse rejectTopUpRequest(Long id) {
        log.info("Rejecting top-up request ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("Top-up request not found with ID: " + id));

        if (!"PENDING".equals(transaction.getStatus())) {
            throw new InvalidRequestException("Only PENDING requests can be rejected. Current status: " + transaction.getStatus());
        }

        // Update transaction status to FAILED
        transaction.setStatus("FAILED");
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Top-up request rejected. ID: {}, User ID: {}", id, transaction.getIdUser());

        return mapToResponse(savedTransaction);
    }

    /**
     * Delete top-up request (Admin only)
     */
    @Transactional
    public void deleteTopUpRequest(Long id) {
        log.info("Deleting top-up request ID: {}", id);

        if (!transactionRepository.existsById(id)) {
            throw new InvalidRequestException("Top-up request not found with ID: " + id);
        }

        transactionRepository.deleteById(id);
        log.info("Top-up request deleted successfully. ID: {}", id);
    }

    /**
     * Map Transaction entity to TopUpResponse DTO
     */
    private TopUpResponse mapToResponse(Transaction transaction) {
        TopUpResponse.TopUpResponseBuilder builder = TopUpResponse.builder()
                .id(transaction.getId())
                .idUser(transaction.getIdUser())
                .transactionMethod(transaction.getTransactionMethod())
                .amount(transaction.getAmount())
                .tnxId(transaction.getTnxId())
                .date(transaction.getDate())
                .status(transaction.getStatus())
                .updatedAt(transaction.getUpdatedAt())
                .durationInSeconds(calculateDuration(transaction.getAmount()));

        // Fetch and include user information if user ID exists
        if (transaction.getIdUser() != null) {
            userRepository.findById(transaction.getIdUser()).ifPresent(user -> {
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
        }

        return builder.build();
    }
}
