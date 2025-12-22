package com.example.voicechanger.service;

import com.example.voicechanger.entity.VoiceMappingHistory;
import com.example.voicechanger.entity.VoiceUserMapping;
import com.example.voicechanger.repository.VoiceMappingHistoryRepository;
import com.example.voicechanger.repository.VoiceUserMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceExpiryCleanupService {

    private final VoiceUserMappingRepository voiceUserMappingRepository;
    private final VoiceMappingHistoryRepository voiceMappingHistoryRepository;

    /**
     * Run cleanup when application starts
     * This ensures any expired mappings are cleaned up immediately on startup
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    @CacheEvict(value = "userVoiceCodesMap", allEntries = true)
    public void cleanupOnApplicationStart() {
        log.info("========================================");
        log.info("🚀 VOICE EXPIRY CLEANUP - APPLICATION STARTUP");
        log.info("========================================");
        log.info("Started at: {}", LocalDateTime.now());

        try {
            Map<String, Object> result = cleanupExpiredMappings();

            log.info("========================================");
            log.info("✅ CLEANUP COMPLETED SUCCESSFULLY");
            log.info("Total Mappings Processed: {}", result.get("processedCount"));
            log.info("Moved to History: {}", result.get("movedToHistoryCount"));
            log.info("Failed: {}", result.get("failedCount"));
            log.info("Cleanup Time: {}", result.get("cleanupTime"));

            if ((int) result.get("failedCount") > 0) {
                log.warn("Errors encountered: {}", result.get("errors"));
            }

            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("❌ CLEANUP FAILED - APPLICATION STARTUP");
            log.error("Error: {}", e.getMessage(), e);
            log.error("========================================");
        }
    }

    /**
     * Scheduled task that runs every day at midnight (12:00 AM) to clean up expired voice mappings
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 0 * * *") // Runs at 12:00 AM (midnight) daily
    @Transactional
    @CacheEvict(value = "userVoiceCodesMap", allEntries = true)
    public void scheduledCleanupExpiredMappings() {
        log.info("========================================");
        log.info("🕛 VOICE EXPIRY CLEANUP - SCHEDULED (MIDNIGHT)");
        log.info("========================================");
        log.info("Started at: {}", LocalDateTime.now());

        try {
            Map<String, Object> result = cleanupExpiredMappings();

            log.info("========================================");
            log.info("✅ CLEANUP COMPLETED SUCCESSFULLY");
            log.info("Total Mappings Processed: {}", result.get("processedCount"));
            log.info("Moved to History: {}", result.get("movedToHistoryCount"));
            log.info("Failed: {}", result.get("failedCount"));
            log.info("Cleanup Time: {}", result.get("cleanupTime"));

            if ((int) result.get("failedCount") > 0) {
                log.warn("Errors encountered: {}", result.get("errors"));
            }

            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("❌ CLEANUP FAILED - SCHEDULED");
            log.error("Error: {}", e.getMessage(), e);
            log.error("========================================");
        }
    }

    /**
     * Manual cleanup of expired voice mappings
     * Can be triggered via API endpoint
     *
     * @return Map with cleanup results
     */
    @Transactional
    @CacheEvict(value = "userVoiceCodesMap", allEntries = true)
    public Map<String, Object> cleanupExpiredMappings() {
        LocalDateTime now = LocalDateTime.now();

        log.info("----------------------------------------");
        log.info("🧹 CLEANUP PROCESS STARTED");
        log.info("Current Time: {}", now);
        log.info("----------------------------------------");

        List<VoiceUserMapping> allMappings = voiceUserMappingRepository.findAll();
        log.info("Total Mappings Found: {}", allMappings.size());

        int processedCount = 0;
        int movedToHistoryCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        for (VoiceUserMapping mapping : allMappings) {
            processedCount++;

            try {
                String expiryReason = determineExpiryReason(mapping, now);

                if (expiryReason != null) {
                    // Mapping is expired - move to history and delete from active table
                    log.info("----------------------------------------");
                    log.info("📦 EXPIRED MAPPING FOUND");
                    log.info("Mapping ID: {}", mapping.getId());
                    log.info("User ID: {}", mapping.getIdUser());
                    log.info("Voice Type ID: {}", mapping.getIdVoiceType());
                    log.info("Expiry Reason: {}", expiryReason);
                    log.info("Trial Expiry: {}", mapping.getTrialExpiryDate());
                    log.info("Subscription Expiry: {}", mapping.getExpiryDate());
                    log.info("Is Purchased: {}", mapping.getIsPurchased());
                    log.info("----------------------------------------");

                    // Create history record
                    VoiceMappingHistory historyRecord = VoiceMappingHistory.fromExpiredMapping(mapping, expiryReason);
                    voiceMappingHistoryRepository.save(historyRecord);
                    log.info("✅ Saved to history table (History ID: {})", historyRecord.getId());

                    // Delete from active table
                    voiceUserMappingRepository.delete(mapping);
                    log.info("✅ Deleted from active table (Mapping ID: {})", mapping.getId());

                    movedToHistoryCount++;
                }

            } catch (Exception e) {
                failedCount++;
                String errorMsg = "Failed to process mapping ID=" + mapping.getId() + ": " + e.getMessage();
                errors.add(errorMsg);
                log.error("----------------------------------------");
                log.error("❌ ERROR PROCESSING MAPPING");
                log.error("Mapping ID: {}", mapping.getId());
                log.error("Error: {}", errorMsg);
                log.error("Stack Trace:", e);
                log.error("----------------------------------------");
            }
        }

        // Prepare result
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("processedCount", processedCount);
        result.put("movedToHistoryCount", movedToHistoryCount);
        result.put("failedCount", failedCount);
        result.put("cleanupTime", LocalDateTime.now());
        result.put("errors", errors);

        log.info("----------------------------------------");
        log.info("🧹 CLEANUP SUMMARY");
        log.info("Total Processed: {}", processedCount);
        log.info("Moved to History: {}", movedToHistoryCount);
        log.info("Failed: {}", failedCount);
        log.info("Active Mappings Remaining: {}", voiceUserMappingRepository.count());
        log.info("Total History Records: {}", voiceMappingHistoryRepository.count());
        log.info("----------------------------------------");

        return result;
    }

    /**
     * Determine if a mapping is expired and return the reason
     *
     * @param mapping - the voice user mapping to check
     * @param now - current timestamp
     * @return expiry reason string or null if not expired
     */
    private String determineExpiryReason(VoiceUserMapping mapping, LocalDateTime now) {
        LocalDateTime trialExpiry = mapping.getTrialExpiryDate();
        LocalDateTime subscriptionExpiry = mapping.getExpiryDate();

        boolean trialExpired = trialExpiry != null && trialExpiry.isBefore(now);
        boolean subscriptionExpired = subscriptionExpiry != null && subscriptionExpiry.isBefore(now);

        // Determine expiry reason
        if (trialExpiry != null && subscriptionExpiry != null) {
            // Has both trial and subscription dates
            if (trialExpired && subscriptionExpired) {
                return "BOTH_EXPIRED";
            } else if (trialExpired && !subscriptionExpired) {
                // Trial expired but subscription is still active - NOT expired
                return null;
            } else if (!trialExpired && subscriptionExpired) {
                // Subscription expired but trial is still active - NOT expired
                return null;
            } else {
                // Both are still active
                return null;
            }
        } else if (trialExpiry != null) {
            // Only has trial date
            return trialExpired ? "TRIAL_EXPIRED" : null;
        } else if (subscriptionExpiry != null) {
            // Only has subscription date
            return subscriptionExpired ? "SUBSCRIPTION_EXPIRED" : null;
        } else {
            // No expiry dates - permanent access, NOT expired
            return null;
        }
    }

    /**
     * Get cleanup statistics
     *
     * @return Map with various statistics
     */
    public Map<String, Object> getCleanupStatistics() {
        log.info("📊 Getting cleanup statistics...");

        Map<String, Object> stats = new HashMap<>();

        // Total history records
        long totalHistoryRecords = voiceMappingHistoryRepository.count();
        stats.put("totalHistoryRecords", totalHistoryRecords);

        // Count by expiry reason
        List<Object[]> reasonCounts = voiceMappingHistoryRepository.countByExpiryReason();
        Map<String, Long> reasonMap = new HashMap<>();
        for (Object[] row : reasonCounts) {
            String reason = (String) row[0];
            Long count = (Long) row[1];
            reasonMap.put(reason, count);
        }
        stats.put("countByExpiryReason", reasonMap);

        // Recent expired (last 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<VoiceMappingHistory> recentExpired = voiceMappingHistoryRepository.findRecentExpired(sevenDaysAgo);
        stats.put("recentExpiredCount", recentExpired.size());

        // Current active mappings count
        long activeMappingsCount = voiceUserMappingRepository.count();
        stats.put("currentActiveMappings", activeMappingsCount);

        log.info("📊 Statistics: Total history={}, Active={}, Recent expired (7 days)={}",
                totalHistoryRecords, activeMappingsCount, recentExpired.size());

        return stats;
    }

    /**
     * Get expired mapping history for a specific user
     *
     * @param userId - the user ID
     * @return List of history records with details
     */
    public List<VoiceMappingHistory> getHistoryForUser(Long userId) {
        log.info("📜 Getting history for user ID: {}", userId);
        return voiceMappingHistoryRepository.findByUserWithDetails(userId);
    }

    /**
     * Get all history records with full details
     *
     * @return List of all history records
     */
    public List<VoiceMappingHistory> getAllHistory() {
        log.info("📜 Getting all history records");
        return voiceMappingHistoryRepository.findAllWithDetails();
    }

    /**
     * Preview what would be cleaned up without actually deleting
     *
     * @return Map with preview information
     */
    public Map<String, Object> previewCleanup() {
        log.info("👁️ Previewing cleanup (dry run)...");

        LocalDateTime now = LocalDateTime.now();
        List<VoiceUserMapping> allMappings = voiceUserMappingRepository.findAll();

        List<Map<String, Object>> toBeCleanedUp = new ArrayList<>();
        int trialExpiredCount = 0;
        int subscriptionExpiredCount = 0;
        int bothExpiredCount = 0;

        for (VoiceUserMapping mapping : allMappings) {
            String expiryReason = determineExpiryReason(mapping, now);

            if (expiryReason != null) {
                Map<String, Object> mappingInfo = new HashMap<>();
                mappingInfo.put("id", mapping.getId());
                mappingInfo.put("userId", mapping.getIdUser());
                mappingInfo.put("voiceTypeId", mapping.getIdVoiceType());
                mappingInfo.put("isPurchased", mapping.getIsPurchased());
                mappingInfo.put("trialExpiryDate", mapping.getTrialExpiryDate());
                mappingInfo.put("expiryDate", mapping.getExpiryDate());
                mappingInfo.put("expiryReason", expiryReason);

                toBeCleanedUp.add(mappingInfo);

                switch (expiryReason) {
                    case "TRIAL_EXPIRED" -> trialExpiredCount++;
                    case "SUBSCRIPTION_EXPIRED" -> subscriptionExpiredCount++;
                    case "BOTH_EXPIRED" -> bothExpiredCount++;
                }
            }
        }

        Map<String, Object> preview = new HashMap<>();
        preview.put("totalMappingsToCleanUp", toBeCleanedUp.size());
        preview.put("trialExpiredCount", trialExpiredCount);
        preview.put("subscriptionExpiredCount", subscriptionExpiredCount);
        preview.put("bothExpiredCount", bothExpiredCount);
        preview.put("mappingsToCleanUp", toBeCleanedUp);
        preview.put("previewTime", LocalDateTime.now());

        log.info("👁️ Preview: {} mappings would be moved to history", toBeCleanedUp.size());

        return preview;
    }
}
