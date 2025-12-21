package com.example.voicechanger.service.esl;

import com.example.voicechanger.entity.CallHistory;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.repository.CallHistoryRepository;
import com.example.voicechanger.repository.UserRepository;
import com.example.voicechanger.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TalkTimeService {

    private final EslService eslService;
    private final BalanceService balanceService;
    private final UserRepository userRepository;
    private final CallHistoryRepository callHistoryRepository;

    // Cache: aparty (username) -> balance remain_amount
    private final Map<String, Long> usernameBalanceCache = new ConcurrentHashMap<>();

    // Active sessions: UUID -> SessionInfo
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

    /**
     * Check & reserve talktime before bridging or transferring
     * aParty is the username making the call
     */
    public boolean checkAndReserveTalkTime(String uuid, String aParty, String bParty, String email, String sourceIp, Date startStamp) {
        try {
            log.info("🔍 Checking balance for user: {} | UUID: {} | B-Party: {} | SourceIP: {}",
                    aParty, uuid, bParty, sourceIp);

            // Find user by username (aparty)
            User user = userRepository.findByUsername(aParty).orElse(null);

            if (user == null) {
                log.error("❌ User not found: {} | UUID: {} | Call REJECTED", aParty, uuid);
                createCallHistory(uuid, aParty, bParty, sourceIp, user, "REJECTED", startStamp);
                return false;
            }

            // Check user balance
            Long remainAmount = balanceService.hasBalance(user.getId(), 1L)
                    ? getBalance(user.getId())
                    : 0L;

            log.info("💰 Balance check | User: {} | Balance: {}s | Status: {}",
                    aParty, remainAmount, remainAmount > 0 ? "✅ SUFFICIENT" : "❌ INSUFFICIENT");

            if (remainAmount <= 0) {
                log.warn("❌ Insufficient balance | User: {} | Balance: {}s | UUID: {} | Call REJECTED",
                        aParty, remainAmount, uuid);
                createCallHistory(uuid, aParty, bParty, sourceIp, user, "REJECTED", startStamp);
                return false;
            }

            // Cache balance and reserve session
            usernameBalanceCache.put(aParty, remainAmount);

            LocalDateTime startTime = startStamp != null
                    ? LocalDateTime.ofInstant(startStamp.toInstant(), ZoneId.systemDefault())
                    : LocalDateTime.now();

            activeSessions.put(uuid, new SessionInfo(user.getId(), aParty, bParty, sourceIp, startTime, remainAmount));

            // Create call history record with RESERVED status
            createCallHistory(uuid, aParty, bParty, sourceIp, user, "RESERVED", startStamp);

            log.info("✅ Balance reserved | User: {} | Balance: {}s | UUID: {} | Call ALLOWED",
                    aParty, remainAmount, uuid);

            // Schedule hangup after balance expires
            scheduleCallHangup(uuid, remainAmount);

            return true;

        } catch (Exception e) {
            log.error("❌ Error in checkAndReserveTalkTime | UUID: {} | Error: {}", uuid, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get user balance from database
     */
    private Long getBalance(Long userId) {
        try {
            var balanceResponse = balanceService.getBalanceByUserId(userId);
            return balanceResponse.getRemainAmount();
        } catch (Exception e) {
            log.error("❌ Error getting balance for user ID: {} | Error: {}", userId, e.getMessage());
            return 0L;
        }
    }

    /**
     * Schedule call hangup when balance expires
     */
    private void scheduleCallHangup(String uuid, Long remainAmount) {
        new Thread(() -> {
            try {
                Thread.sleep(remainAmount * 1000L);
                if (activeSessions.containsKey(uuid)) {
                    log.warn("⏰ Balance expired | UUID: {} | Killing call after {}s", uuid, remainAmount);
                    eslService.sendCommand("uuid_kill " + uuid);
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    /**
     * Mark when the call is answered with timestamp from FreeSWITCH
     */
    public void markAnswered(String uuid, Date answerStamp) {
        SessionInfo session = activeSessions.get(uuid);
        if (session != null) {
            LocalDateTime answerTime = LocalDateTime.ofInstant(answerStamp.toInstant(), ZoneId.systemDefault());
            session.setAnswerTime(answerTime);

            log.info("📞 Call answered | User: {} | UUID: {} | AnswerTime: {}",
                    session.getAparty(), uuid, answerTime);

            // Update call history to ANSWERED
            updateCallHistoryStatus(uuid, "ANSWERED", answerTime);
        }
    }

    /**
     * Deduct balance after call hangup
     */
    public void deductTalkTime(String uuid, Date endTime, String hangupCause, String codec) {
        try {
            SessionInfo session = activeSessions.remove(uuid);
            if (session == null) {
                log.warn("⚠️ No active session found for UUID: {} | Deduction skipped", uuid);
                return;
            }

            LocalDateTime endDateTime = LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault());
            LocalDateTime answerTime = session.getAnswerTime();
            long duration = 0;

            log.info("📞 Call hangup | User: {} | UUID: {} | Cause: {} | Codec: {}",
                    session.getAparty(), uuid, hangupCause, codec);

            if (answerTime == null) {
                // Call was never answered - duration should be 0
                duration = 0;
                log.info("⚠️ Call never answered | User: {} | UUID: {} | Duration: 0s | No deduction",
                        session.getAparty(), uuid);

                updateCallHistoryWithHangup(uuid, "FAILED", endDateTime, 0L, hangupCause, codec);
            } else {
                // Calculate duration from answer time to end time with ceiling
                double diffSeconds = java.time.Duration.between(answerTime, endDateTime).toMillis() / 1000.0;
                duration = (long) Math.max(0, Math.ceil(diffSeconds));

                log.info("📊 Call completed | User: {} | UUID: {} | Duration: {}s | Codec: {} | Deducting from balance",
                        session.getAparty(), uuid, duration, codec);

                // Deduct balance
                boolean deducted = balanceService.deductBalance(session.getUserId(), duration);

                if (deducted) {
                    log.info("💰 Balance deducted | User: {} | Amount: {}s | UUID: {} | Status: ✅ SUCCESS",
                            session.getAparty(), duration, uuid);
                } else {
                    log.error("❌ Balance deduction failed | User: {} | Amount: {}s | UUID: {}",
                            session.getAparty(), duration, uuid);
                }

                // Update call history to COMPLETED with hangup cause and codec
                updateCallHistoryComplete(uuid, answerTime, endDateTime, duration, "COMPLETED", hangupCause, codec);
            }

            // Clear cache
            usernameBalanceCache.remove(session.getAparty());

            log.info("✅ Call session closed | User: {} | UUID: {} | Final Duration: {}s | Hangup: {}",
                    session.getAparty(), uuid, duration, hangupCause);

        } catch (Exception e) {
            log.error("❌ Error in deductTalkTime | UUID: {} | Error: {}", uuid, e.getMessage(), e);
        }
    }

    /**
     * Overloaded method for backward compatibility
     */
    public void deductTalkTime(String uuid, Date endTime) {
        deductTalkTime(uuid, endTime, "NORMAL_CLEARING", null);
    }

    /**
     * Create call history record
     */
    private void createCallHistory(String uuid, String aparty, String bparty, String sourceIp,
                                   User user, String status, Date startStamp) {
        try {
            CallHistory callHistory = new CallHistory();
            callHistory.setUuid(uuid);
            callHistory.setAparty(aparty);
            callHistory.setBparty(bparty);
            callHistory.setSourceIp(sourceIp);
            callHistory.setIdUser(user != null ? user.getId() : null);
            callHistory.setStatus(status);
            callHistory.setDuration(0L);

            LocalDateTime createTime = startStamp != null
                    ? LocalDateTime.ofInstant(startStamp.toInstant(), ZoneId.systemDefault())
                    : LocalDateTime.now();
            callHistory.setCreateTime(createTime);

            callHistoryRepository.save(callHistory);
            log.debug("📝 Call history created | UUID: {} | Status: {}", uuid, status);
        } catch (Exception e) {
            log.error("❌ Error creating call history | UUID: {} | Error: {}", uuid, e.getMessage());
        }
    }

    /**
     * Update call history status
     */
    private void updateCallHistoryStatus(String uuid, String status, LocalDateTime time) {
        try {
            callHistoryRepository.findByUuid(uuid).ifPresent(callHistory -> {
                callHistory.setStatus(status);
                if ("ANSWERED".equals(status)) {
                    callHistory.setStartTime(time);
                } else if ("FAILED".equals(status)) {
                    callHistory.setEndTime(time);
                }
                callHistoryRepository.save(callHistory);
                log.debug("📝 Call history updated | UUID: {} | Status: {}", uuid, status);
            });
        } catch (Exception e) {
            log.error("❌ Error updating call history status | UUID: {} | Error: {}", uuid, e.getMessage());
        }
    }

    /**
     * Update call history with hangup details (for failed calls)
     */
    private void updateCallHistoryWithHangup(String uuid, String status, LocalDateTime endTime,
                                            Long duration, String hangupCause, String codec) {
        try {
            callHistoryRepository.findByUuid(uuid).ifPresent(callHistory -> {
                callHistory.setEndTime(endTime);
                callHistory.setDuration(duration);
                callHistory.setStatus(status);
                callHistory.setHangupCause(hangupCause);
                callHistory.setCodec(codec);
                callHistoryRepository.save(callHistory);
                log.debug("📝 Call history updated | UUID: {} | Status: {} | Cause: {} | Codec: {}",
                        uuid, status, hangupCause, codec);
            });
        } catch (Exception e) {
            log.error("❌ Error updating call history | UUID: {} | Error: {}", uuid, e.getMessage());
        }
    }

    /**
     * Update call history with complete call details
     */
    private void updateCallHistoryComplete(String uuid, LocalDateTime startTime, LocalDateTime endTime,
                                          Long duration, String status, String hangupCause, String codec) {
        try {
            callHistoryRepository.findByUuid(uuid).ifPresent(callHistory -> {
                callHistory.setStartTime(startTime);
                callHistory.setEndTime(endTime);
                callHistory.setDuration(duration);
                callHistory.setStatus(status);
                callHistory.setHangupCause(hangupCause);
                callHistory.setCodec(codec);
                callHistoryRepository.save(callHistory);
                log.debug("📝 Call history completed | UUID: {} | Duration: {}s | Status: {} | Cause: {} | Codec: {}",
                        uuid, duration, status, hangupCause, codec);
            });
        } catch (Exception e) {
            log.error("❌ Error updating call history | UUID: {} | Error: {}", uuid, e.getMessage());
        }
    }

    /**
     * Session info for active calls
     */
    private static class SessionInfo {
        private final Long userId;
        private final String aparty;
        private final String bparty;
        private final String sourceIp;
        private final LocalDateTime createTime;
        private LocalDateTime answerTime;
        private final Long maxDuration;

        public SessionInfo(Long userId, String aparty, String bparty, String sourceIp,
                          LocalDateTime createTime, Long maxDuration) {
            this.userId = userId;
            this.aparty = aparty;
            this.bparty = bparty;
            this.sourceIp = sourceIp;
            this.createTime = createTime;
            this.maxDuration = maxDuration;
        }

        public Long getUserId() { return userId; }
        public String getAparty() { return aparty; }
        public String getBparty() { return bparty; }
        public String getSourceIp() { return sourceIp; }
        public LocalDateTime getCreateTime() { return createTime; }
        public LocalDateTime getAnswerTime() { return answerTime; }
        public void setAnswerTime(LocalDateTime answerTime) { this.answerTime = answerTime; }
        public Long getMaxDuration() { return maxDuration; }
    }
}
