package com.example.voicechanger.controller;

import com.example.voicechanger.entity.VoiceMappingHistory;
import com.example.voicechanger.service.VoiceExpiryCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/voice-cleanup")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VoiceExpiryCleanupController {

    private final VoiceExpiryCleanupService voiceExpiryCleanupService;

    /**
     * Manually trigger cleanup of expired voice mappings
     * POST /api/voice-cleanup/run
     *
     * @return Cleanup results
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runCleanup() {
        log.info("API call: Manual cleanup of expired voice mappings");

        try {
            Map<String, Object> result = voiceExpiryCleanupService.cleanupExpiredMappings();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during manual cleanup", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Preview what would be cleaned up without actually deleting
     * GET /api/voice-cleanup/preview
     *
     * @return Preview of mappings to be cleaned up
     */
    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewCleanup() {
        log.info("API call: Preview cleanup");

        try {
            Map<String, Object> preview = voiceExpiryCleanupService.previewCleanup();
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            log.error("Error during cleanup preview", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cleanup statistics
     * GET /api/voice-cleanup/statistics
     *
     * @return Statistics about cleanup history
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("API call: Get cleanup statistics");

        try {
            Map<String, Object> stats = voiceExpiryCleanupService.getCleanupStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting cleanup statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all history records
     * GET /api/voice-cleanup/history
     *
     * @return List of all history records
     */
    @GetMapping("/history")
    public ResponseEntity<List<VoiceMappingHistory>> getAllHistory() {
        log.info("API call: Get all history records");

        try {
            List<VoiceMappingHistory> history = voiceExpiryCleanupService.getAllHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting history records", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get history records for a specific user
     * GET /api/voice-cleanup/history/user/{userId}
     *
     * @param userId - the user ID
     * @return List of history records for the user
     */
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<VoiceMappingHistory>> getHistoryForUser(@PathVariable Long userId) {
        log.info("API call: Get history for user ID: {}", userId);

        try {
            List<VoiceMappingHistory> history = voiceExpiryCleanupService.getHistoryForUser(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting history for user: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
