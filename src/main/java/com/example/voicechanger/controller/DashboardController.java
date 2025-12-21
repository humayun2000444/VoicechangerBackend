package com.example.voicechanger.controller;

import com.example.voicechanger.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final UserRepository userRepository;
    private final VoiceTypeRepository voiceTypeRepository;
    private final TransactionRepository transactionRepository;
    private final CallHistoryRepository callHistoryRepository;

    /**
     * Get dashboard statistics/counts
     * Returns counts for users, voice types, transactions, and call history
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Fetching dashboard statistics");

        Map<String, Object> stats = new HashMap<>();

        try {
            // Get counts
            long totalUsers = userRepository.count();
            long totalVoiceTypes = voiceTypeRepository.count();
            long totalTopUps = transactionRepository.count();
            long totalCallHistory = callHistoryRepository.count();

            stats.put("totalUsers", totalUsers);
            stats.put("totalVoiceTypes", totalVoiceTypes);
            stats.put("totalTopUps", totalTopUps);
            stats.put("totalCallHistory", totalCallHistory);
            stats.put("success", true);

            log.info("Dashboard stats - Users: {}, Voice Types: {}, Top-Ups: {}, Call History: {}",
                    totalUsers, totalVoiceTypes, totalTopUps, totalCallHistory);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard statistics", e);
            stats.put("success", false);
            stats.put("message", "Error fetching dashboard statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(stats);
        }
    }
}
