package com.example.voicechanger.service;

import com.example.voicechanger.dto.BalanceResponse;
import com.example.voicechanger.entity.Balance;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.repository.BalanceRepository;
import com.example.voicechanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;

    @Transactional
    public BalanceResponse getBalanceByUserId(Long userId) {
        log.info("Fetching balance for user ID: {}", userId);

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Balance balance = balanceRepository.findByIdUser(userId)
                .orElseGet(() -> {
                    log.info("No balance found for user ID: {}, creating new balance", userId);
                    return createInitialBalance(userId);
                });

        return convertToResponse(balance, user);
    }

    @Transactional
    public Balance createInitialBalance(Long userId) {
        log.info("Creating initial balance for user ID: {}", userId);

        Balance balance = new Balance();
        balance.setIdUser(userId);
        balance.setPurchaseAmount(0L);
        balance.setLastUsedAmount(0L);
        balance.setTotalUsedAmount(0L);
        balance.setRemainAmount(0L);

        return balanceRepository.save(balance);
    }

    @Transactional
    public void addBalance(Long userId, Long durationInSeconds) {
        log.info("Adding {} seconds to balance for user ID: {}", durationInSeconds, userId);

        Balance balance = balanceRepository.findByIdUser(userId)
                .orElseGet(() -> createInitialBalance(userId));

        balance.setPurchaseAmount(balance.getPurchaseAmount() + durationInSeconds);
        balance.setRemainAmount(balance.getRemainAmount() + durationInSeconds);

        balanceRepository.save(balance);
        log.info("Balance updated for user ID: {}. New remain amount: {} seconds", userId, balance.getRemainAmount());
    }

    @Transactional
    public boolean deductBalance(Long userId, Long durationInSeconds) {
        log.info("Attempting to deduct {} seconds from balance for user ID: {}", durationInSeconds, userId);

        Balance balance = balanceRepository.findByIdUser(userId)
                .orElseThrow(() -> new RuntimeException("No balance found for user ID: " + userId));

        if (balance.getRemainAmount() < durationInSeconds) {
            log.warn("Insufficient balance for user ID: {}. Required: {} seconds, Available: {} seconds",
                    userId, durationInSeconds, balance.getRemainAmount());
            return false;
        }

        balance.setLastUsedAmount(durationInSeconds);
        balance.setTotalUsedAmount(balance.getTotalUsedAmount() + durationInSeconds);
        balance.setRemainAmount(balance.getRemainAmount() - durationInSeconds);

        balanceRepository.save(balance);
        log.info("Balance deducted for user ID: {}. Remaining: {} seconds", userId, balance.getRemainAmount());

        return true;
    }

    @Transactional(readOnly = true)
    public boolean hasBalance(Long userId, Long durationInSeconds) {
        Balance balance = balanceRepository.findByIdUser(userId)
                .orElse(null);

        if (balance == null) {
            return false;
        }

        return balance.getRemainAmount() >= durationInSeconds;
    }

    private BalanceResponse convertToResponse(Balance balance, User user) {
        return BalanceResponse.builder()
                .id(balance.getId())
                .purchaseAmount(balance.getPurchaseAmount())
                .lastUsedAmount(balance.getLastUsedAmount())
                .totalUsedAmount(balance.getTotalUsedAmount())
                .remainAmount(balance.getRemainAmount())
                .userId(balance.getIdUser())
                .username(user.getUsername())
                .build();
    }
}
