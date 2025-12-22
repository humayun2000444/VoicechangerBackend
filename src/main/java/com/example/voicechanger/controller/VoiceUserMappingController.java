package com.example.voicechanger.controller;

import com.example.voicechanger.entity.VoiceUserMapping;
import com.example.voicechanger.service.VoiceUserMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/voice-user-mapping")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VoiceUserMappingController {

    private final VoiceUserMappingService voiceUserMappingService;

    /**
     * Get the complete HashMap of username to voice codes
     * GET /api/voice-user-mapping/map
     *
     * @return HashMap<String, List<String>> - username to voice codes
     */
    @GetMapping("/map")
    public ResponseEntity<Map<String, List<String>>> getUserVoiceCodesMap() {
        log.info("API call: Get user-to-voice-codes HashMap");

        try {
            Map<String, List<String>> userVoiceCodesMap = voiceUserMappingService.getUserVoiceCodesMap();
            return ResponseEntity.ok(userVoiceCodesMap);
        } catch (Exception e) {
            log.error("Error getting user-to-voice-codes HashMap", e);
            return ResponseEntity.internalServerError().body(new HashMap<>());
        }
    }

    /**
     * Get voice codes for a specific user
     * GET /api/voice-user-mapping/user/{username}/codes
     *
     * @param username - the username to lookup
     * @return List of voice codes
     */
    @GetMapping("/user/{username}/codes")
    public ResponseEntity<Map<String, Object>> getVoiceCodesForUser(@PathVariable String username) {
        log.info("API call: Get voice codes for user: {}", username);

        try {
            List<String> voiceCodes = voiceUserMappingService.getVoiceCodesForUser(username);

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("voiceCodes", voiceCodes);
            response.put("count", voiceCodes.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting voice codes for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all usernames that have access to a specific voice code
     * GET /api/voice-user-mapping/voice-code/{voiceCode}/users
     *
     * @param voiceCode - the voice code to lookup
     * @return List of usernames
     */
    @GetMapping("/voice-code/{voiceCode}/users")
    public ResponseEntity<Map<String, Object>> getUsernamesForVoiceCode(@PathVariable String voiceCode) {
        log.info("API call: Get usernames for voice code: {}", voiceCode);

        try {
            List<String> usernames = voiceUserMappingService.getUsernamesForVoiceCode(voiceCode);

            Map<String, Object> response = new HashMap<>();
            response.put("voiceCode", voiceCode);
            response.put("usernames", usernames);
            response.put("count", usernames.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting usernames for voice code: {}", voiceCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if a user has access to a specific voice code
     * GET /api/voice-user-mapping/check-access?username={username}&voiceCode={code}
     *
     * @param username - the username to check
     * @param voiceCode - the voice code to check
     * @return boolean indicating access
     */
    @GetMapping("/check-access")
    public ResponseEntity<Map<String, Object>> checkUserAccess(
            @RequestParam String username,
            @RequestParam String voiceCode) {
        log.info("API call: Check access for user: {} to voice code: {}", username, voiceCode);

        try {
            boolean hasAccess = voiceUserMappingService.hasUserAccessToVoiceCode(username, voiceCode);

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("voiceCode", voiceCode);
            response.put("hasAccess", hasAccess);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking access for user: {} to voice code: {}", username, voiceCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics about voice access
     * GET /api/voice-user-mapping/statistics
     *
     * @return Map with statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("API call: Get voice access statistics");

        try {
            Map<String, Object> stats = voiceUserMappingService.getVoiceAccessStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting voice access statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get detailed mapping information for a user
     * GET /api/voice-user-mapping/user/{username}/details
     *
     * @param username - the username to lookup
     * @return List of VoiceUserMapping entities with details
     */
    @GetMapping("/user/{username}/details")
    public ResponseEntity<Map<String, Object>> getDetailedMappingsForUser(@PathVariable String username) {
        log.info("API call: Get detailed mappings for user: {}", username);

        try {
            List<VoiceUserMapping> mappings = voiceUserMappingService.getDetailedMappingsForUser(username);

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("mappings", mappings);
            response.put("count", mappings.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting detailed mappings for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clear the cache (force refresh)
     * POST /api/voice-user-mapping/cache/clear
     *
     * @return Success message
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        log.info("API call: Clear voice user mapping cache");

        try {
            voiceUserMappingService.clearCache();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache cleared successfully");
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing cache", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Print the HashMap to console (for debugging)
     * GET /api/voice-user-mapping/debug/print
     *
     * @return Success message
     */
    @GetMapping("/debug/print")
    public ResponseEntity<Map<String, String>> printHashMap() {
        log.info("API call: Print user-to-voice-codes HashMap");

        try {
            voiceUserMappingService.printUserVoiceCodesMap();

            Map<String, String> response = new HashMap<>();
            response.put("message", "HashMap printed to console logs");
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error printing HashMap", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
