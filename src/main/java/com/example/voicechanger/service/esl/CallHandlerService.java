package com.example.voicechanger.service.esl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CallHandlerService {

    private final TalkTimeService talkTimeService;
    private final CallTransferService callTransferService;
    private final EslService eslService;

    // Cache to store active bridge information with event headers
    private final Map<String, Map<String, String>> activeBridges = new ConcurrentHashMap<>();

    public CallHandlerService(TalkTimeService talkTimeService,
                              CallTransferService callTransferService,
                              EslService eslService) {
        this.talkTimeService = talkTimeService;
        this.callTransferService = callTransferService;
        this.eslService = eslService;
    }

    public void handleBridge(Map<String, String> headers) {
        String aLegUuid = headers.get("Bridge-A-Unique-ID");
        String bLegUuid = headers.get("Bridge-B-Unique-ID");
        String userName = headers.getOrDefault("variable_user_name", headers.get("Caller-ANI"));

        log.info("🔗 CHANNEL_BRIDGE event | A-Leg={}, B-Leg={}, User={}", aLegUuid, bLegUuid, userName);

        if (bLegUuid == null || userName == null) {
            log.warn("⚠️ Missing required bridge parameters - A-Leg={}, B-Leg={}, User={}", aLegUuid, bLegUuid, userName);
            return;
        }

        // Store bridge information in cache
        activeBridges.put(bLegUuid, headers);
        log.debug("📝 Stored active bridge info for B-Leg: {}", bLegUuid);

        try {
            applyVoiceChanger(bLegUuid, userName);
        } catch (Exception e) {
            log.error("❌ Error applying voice changer for bridge {}: {}", bLegUuid, e.getMessage(), e);
        }
    }

    public void handlePark(Map<String, String> headers) {
        String uuid = headers.get("Unique-ID");
        String calledNumber = headers.get("Caller-Destination-Number");
        String userName = headers.getOrDefault("variable_user_name", headers.get("Caller-ANI"));

        log.info("📌 Call parked | UUID={}, Destination={}, User={}", uuid, calledNumber, userName);

        if (uuid == null || calledNumber == null || userName == null) {
            log.warn("⚠️ Missing required park parameters - UUID={}, Destination={}, User={}", uuid, calledNumber, userName);
            return;
        }

        String[] parts = userName.split("_");
        if (parts.length < 3) {
            log.warn("⚠️ Invalid userName format: {} (expected format: aParty_bParty_email)", userName);
            return;
        }

        String aParty = parts[0], bParty = parts[1], email = parts[2];
        log.debug("📋 Parsed user data - A-Party={}, B-Party={}, Email={}", aParty, bParty, email);

        // Extract source IP from headers (FreeSWITCH provides this in multiple variables)
        String sourceIp = headers.getOrDefault("variable_sip_received_ip",
                          headers.getOrDefault("variable_sip_network_ip",
                          headers.getOrDefault("variable_sip_req_host", "127.0.0.1")));
        log.debug("🌐 Source IP detected: {}", sourceIp);

        try {
            if (!talkTimeService.checkAndReserveTalkTime(uuid, aParty, bParty, email, sourceIp)) {
                log.warn("❌ Call {} dropped from park due to insufficient talk time", uuid);
                eslService.sendCommand("uuid_kill " + uuid);
                return;
            }

            callTransferService.transferToDefault(uuid, calledNumber);
            log.info("✅ Call {} successfully transferred after park validation", uuid);
        } catch (Exception e) {
            log.error("❌ Error handling park for call {}: {}", uuid, e.getMessage(), e);
        }
    }

    public void handleAnswer(Map<String, String> headers) {
        String uuid = headers.get("Unique-ID");
        String caller = headers.get("Caller-Caller-ID-Number");

        log.info("✅ Call answered | Caller={}, UUID={}", caller, uuid);

        try {
            talkTimeService.markAnswered(uuid);
            log.debug("📝 Call {} marked as answered in talk time service", uuid);
        } catch (Exception e) {
            log.error("❌ Error marking call {} as answered: {}", uuid, e.getMessage(), e);
        }
    }

    public void handleHangup(Map<String, String> headers) {
        String hangupUuid = headers.get("Unique-ID");
        String caller = headers.get("Caller-Caller-ID-Number");
        String direction = headers.get("Call-Direction");
        String hangupCause = headers.get("Hangup-Cause");

        log.info("❌ Call hangup | Caller={}, Direction={}, Cause={}", caller, direction, hangupCause);

        // Remove from active bridges cache
        activeBridges.remove(hangupUuid);
        log.debug("🗑️ Removed bridge info for UUID: {}", hangupUuid);

        try {
            if ("inbound".equalsIgnoreCase(direction)) {
                talkTimeService.deductTalkTime(hangupUuid, new Date());
            }
        } catch (Exception e) {
            log.error("❌ Error processing hangup for call {}: {}", hangupUuid, e.getMessage(), e);
        }
    }

    public void handleUnpark(Map<String, String> headers) {
        String uuid = headers.get("Unique-ID");
        String caller = headers.get("Caller-Caller-ID-Number");

        log.info("📤 Call unparked | Caller={}, UUID={}", caller, uuid);
        log.debug("🚀 Call {} is now active and being processed", uuid);
    }

    private void applyVoiceChanger(String uuid, String userName) {
        String suffix = userName.substring(userName.lastIndexOf("_") + 1);

        switch (suffix) {
            case "901" -> {
                log.info("🎭 Applying standard voice changer for call {}", uuid);
                callTransferService.startVoiceChanger(uuid);
            }
            case "902" -> {
                log.info("👹 Applying monster voice preset for call {}", uuid);
                callTransferService.startVoiceChanger(uuid);
                callTransferService.setVoiceChangerParams(uuid, "-15", "-4", "300");
            }
            case "903" -> {
                log.info("👶 Applying child voice preset for call {}", uuid);
                callTransferService.startVoiceChanger(uuid);
                callTransferService.setVoiceChangerParams(uuid, "8", "4", "120");
            }
            case "904" -> {
                log.info("📞 Normal call bridge for {} - no voice changer applied", uuid);
            }
            default -> {
                log.warn("⚠️ Unknown voice changer suffix '{}' for user {} - defaulting to normal call", suffix, userName);
            }
        }
    }

    /**
     * Find B-Leg UUID for a given email by searching through active bridges
     * @param email Email to search for (e.g., "humu2@gmail.com")
     * @return B-Leg UUID if found, null otherwise
     */
    public String findBLegUuidByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        // Convert email format: humu2@gmail.com -> humu2-gmail-com
        String normalizedEmail = email.replace("@", "-").replace(".", "-").toLowerCase();

        for (Map.Entry<String, Map<String, String>> entry : activeBridges.entrySet()) {
            String bLegUuid = entry.getKey();
            Map<String, String> headers = entry.getValue();

            String userName = headers.getOrDefault("variable_user_name", headers.get("Caller-ANI"));
            if (userName == null || userName.isEmpty()) {
                continue;
            }

            // Parse userName format: 1003_1006_humu2-gmail-com_901
            String[] parts = userName.split("_");
            if (parts.length >= 3) {
                String extractedEmail = parts[2].toLowerCase(); // third part = email in format humu2-gmail-com

                if (extractedEmail.equals(normalizedEmail)) {
                    log.debug("✅ Found matching bridge for email {} -> B-Leg UUID: {}", email, bLegUuid);
                    return bLegUuid;
                }
            }
        }

        log.debug("❌ No active bridge found for email: {}", email);
        return null;
    }

    /**
     * Get count of active bridges
     * @return Number of active bridges
     */
    public int getActiveBridgeCount() {
        return activeBridges.size();
    }
}
