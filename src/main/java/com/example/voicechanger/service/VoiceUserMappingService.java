package com.example.voicechanger.service;

import com.example.voicechanger.entity.VoiceUserMapping;
import com.example.voicechanger.repository.VoiceUserMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceUserMappingService {

    private final VoiceUserMappingRepository voiceUserMappingRepository;

    /**
     * Builds a HashMap where:
     * - Key: username (from users table)
     * - Value: List of voice type codes that the user has active access to
     *
     * Only includes active mappings (not expired trials or subscriptions)
     *
     * @return HashMap<String, List<String>> - username to voice codes mapping
     */
    @Cacheable(value = "userVoiceCodesMap", unless = "#result == null || #result.isEmpty()")
    public Map<String, List<String>> getUserVoiceCodesMap() {
        log.info("Building user-to-voice-codes HashMap...");

        LocalDateTime now = LocalDateTime.now();
        Map<String, List<String>> userVoiceCodesMap = new HashMap<>();

        try {
            // Get all voice user mappings with user and voiceType eagerly fetched
            List<VoiceUserMapping> allMappings = voiceUserMappingRepository.findAllWithUserAndVoiceType();

            log.info("Found {} total voice user mappings", allMappings.size());

            // Filter active mappings and group by username
            for (VoiceUserMapping mapping : allMappings) {
                // Check if mapping is active (not expired)
                if (isActiveMappingByExpiry(mapping, now)) {
                    // Get username from user entity
                    if (mapping.getUser() != null && mapping.getUser().getUsername() != null) {
                        String username = mapping.getUser().getUsername();

                        // Get voice code from voice type entity
                        if (mapping.getVoiceType() != null && mapping.getVoiceType().getCode() != null) {
                            String voiceCode = mapping.getVoiceType().getCode();

                            // Add to map
                            userVoiceCodesMap.computeIfAbsent(username, k -> new ArrayList<>())
                                           .add(voiceCode);
                        }
                    }
                }
            }

            // Sort voice codes for each user for consistency
            userVoiceCodesMap.forEach((username, codes) -> Collections.sort(codes));

            log.info("Built HashMap with {} users having active voice access", userVoiceCodesMap.size());

            return userVoiceCodesMap;

        } catch (Exception e) {
            log.error("Error building user-to-voice-codes HashMap", e);
            return new HashMap<>();
        }
    }

    /**
     * Gets voice codes for a specific user
     *
     * @param username - the username to lookup
     * @return List of voice codes the user has access to, or empty list if none
     */
    public List<String> getVoiceCodesForUser(String username) {
        Map<String, List<String>> userVoiceCodesMap = getUserVoiceCodesMap();
        return userVoiceCodesMap.getOrDefault(username, new ArrayList<>());
    }

    /**
     * Gets all usernames that have access to a specific voice code
     *
     * @param voiceCode - the voice code to lookup
     * @return List of usernames that have access to this voice
     */
    public List<String> getUsernamesForVoiceCode(String voiceCode) {
        Map<String, List<String>> userVoiceCodesMap = getUserVoiceCodesMap();

        return userVoiceCodesMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(voiceCode))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a user has access to a specific voice code
     *
     * @param username - the username to check
     * @param voiceCode - the voice code to check
     * @return true if user has active access, false otherwise
     */
    public boolean hasUserAccessToVoiceCode(String username, String voiceCode) {
        List<String> voiceCodes = getVoiceCodesForUser(username);
        return voiceCodes.contains(voiceCode);
    }

    /**
     * Gets statistics about voice access
     *
     * @return Map with various statistics
     */
    public Map<String, Object> getVoiceAccessStatistics() {
        Map<String, List<String>> userVoiceCodesMap = getUserVoiceCodesMap();
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsersWithVoiceAccess", userVoiceCodesMap.size());
        stats.put("totalActiveVoiceMappings",
                 userVoiceCodesMap.values().stream().mapToInt(List::size).sum());

        // Most popular voice codes
        Map<String, Long> voiceCodeCounts = userVoiceCodesMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));

        stats.put("voiceCodeUsage", voiceCodeCounts);

        // User with most voice access
        Optional<Map.Entry<String, List<String>>> maxEntry = userVoiceCodesMap.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()));

        maxEntry.ifPresent(entry -> {
            stats.put("userWithMostAccess", entry.getKey());
            stats.put("maxVoiceAccessCount", entry.getValue().size());
        });

        return stats;
    }

    /**
     * Clears the cache - useful when mappings are updated
     */
    @CacheEvict(value = "userVoiceCodesMap", allEntries = true)
    public void clearCache() {
        log.info("Cleared user-to-voice-codes cache");
    }

    /**
     * Scheduled task to refresh cache every hour
     * This ensures the cache doesn't get stale with expired subscriptions
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @CacheEvict(value = "userVoiceCodesMap", allEntries = true)
    public void scheduledCacheRefresh() {
        log.info("Scheduled cache refresh triggered for user-to-voice-codes map");
    }

    /**
     * Helper method to check if a mapping is active based on expiry dates
     *
     * A mapping is active if:
     * 1. Both trial and subscription expiry are null (permanent access), OR
     * 2. Trial expiry is null or in future AND subscription expiry is null or in future
     *
     * @param mapping - the voice user mapping to check
     * @param now - current timestamp
     * @return true if mapping is active, false if expired
     */
    private boolean isActiveMappingByExpiry(VoiceUserMapping mapping, LocalDateTime now) {
        LocalDateTime trialExpiry = mapping.getTrialExpiryDate();
        LocalDateTime subscriptionExpiry = mapping.getExpiryDate();

        // Check trial expiry (if present)
        boolean trialActive = trialExpiry == null || trialExpiry.isAfter(now);

        // Check subscription expiry (if present)
        boolean subscriptionActive = subscriptionExpiry == null || subscriptionExpiry.isAfter(now);

        // Mapping is active if EITHER trial is active OR subscription is active
        // If trial expired but subscription is active, still active
        // If subscription expired but trial is active, still active
        // If both present and both expired, not active

        if (trialExpiry != null && subscriptionExpiry != null) {
            // Both present - at least one must be active
            return trialActive || subscriptionActive;
        } else if (trialExpiry != null) {
            // Only trial present
            return trialActive;
        } else if (subscriptionExpiry != null) {
            // Only subscription present
            return subscriptionActive;
        } else {
            // Neither present - permanent access
            return true;
        }
    }

    /**
     * Gets detailed mapping information for a user
     *
     * @param username - the username to lookup
     * @return List of VoiceUserMapping entities
     */
    public List<VoiceUserMapping> getDetailedMappingsForUser(String username) {
        LocalDateTime now = LocalDateTime.now();

        return voiceUserMappingRepository.findAllWithUserAndVoiceType().stream()
                .filter(mapping -> mapping.getUser() != null
                        && username.equals(mapping.getUser().getUsername()))
                .filter(mapping -> isActiveMappingByExpiry(mapping, now))
                .collect(Collectors.toList());
    }

    /**
     * Prints the HashMap to console (for debugging)
     */
    public void printUserVoiceCodesMap() {
        Map<String, List<String>> map = getUserVoiceCodesMap();

        log.info("=== User to Voice Codes HashMap ===");
        log.info("Total users: {}", map.size());

        map.forEach((username, codes) -> {
            log.info("User: {} -> Voice Codes: {}", username, codes);
        });

        log.info("=== End of HashMap ===");
    }
}
