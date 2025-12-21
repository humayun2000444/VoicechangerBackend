package com.example.voicechanger.service;

import com.example.voicechanger.dto.CallHistoryResponse;
import com.example.voicechanger.dto.UserResponse;
import com.example.voicechanger.entity.CallHistory;
import com.example.voicechanger.entity.Role;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.exception.InvalidRequestException;
import com.example.voicechanger.repository.CallHistoryRepository;
import com.example.voicechanger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallHistoryService {

    private final CallHistoryRepository callHistoryRepository;
    private final UserRepository userRepository;

    /**
     * Get call history for the authenticated user
     */
    public List<CallHistoryResponse> getMyCallHistory() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching call history for authenticated user: {}", username);
        return callHistoryRepository.findByAparty(username).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all call history records
     */
    public List<CallHistoryResponse> getAllCallHistory() {
        log.info("Fetching all call history records");
        return callHistoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get call history by ID
     */
    public CallHistoryResponse getCallHistoryById(Long id) {
        log.info("Fetching call history by ID: {}", id);
        CallHistory callHistory = callHistoryRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("Call history not found with ID: " + id));
        return mapToResponse(callHistory);
    }

    /**
     * Get call history by UUID
     */
    public CallHistoryResponse getCallHistoryByUuid(String uuid) {
        log.info("Fetching call history by UUID: {}", uuid);
        CallHistory callHistory = callHistoryRepository.findByUuid(uuid)
                .orElseThrow(() -> new InvalidRequestException("Call history not found with UUID: " + uuid));
        return mapToResponse(callHistory);
    }

    /**
     * Get call history by user ID
     */
    public List<CallHistoryResponse> getCallHistoryByUserId(Long userId) {
        log.info("Fetching call history for user ID: {}", userId);
        return callHistoryRepository.findByIdUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get call history by aparty (calling party)
     */
    public List<CallHistoryResponse> getCallHistoryByAparty(String aparty) {
        log.info("Fetching call history for aparty: {}", aparty);
        return callHistoryRepository.findByAparty(aparty).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get call history by date range
     */
    public List<CallHistoryResponse> getCallHistoryByDateRange(LocalDateTime start, LocalDateTime end) {
        log.info("Fetching call history between {} and {}", start, end);
        return callHistoryRepository.findByCreateTimeBetween(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get call history by status
     */
    public List<CallHistoryResponse> getCallHistoryByStatus(String status) {
        log.info("Fetching call history with status: {}", status);
        return callHistoryRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete call history by ID
     */
    public void deleteCallHistory(Long id) {
        log.info("Deleting call history ID: {}", id);
        if (!callHistoryRepository.existsById(id)) {
            throw new InvalidRequestException("Call history not found with ID: " + id);
        }
        callHistoryRepository.deleteById(id);
        log.info("Call history deleted successfully. ID: {}", id);
    }

    /**
     * Map CallHistory entity to CallHistoryResponse DTO
     */
    private CallHistoryResponse mapToResponse(CallHistory callHistory) {
        CallHistoryResponse.CallHistoryResponseBuilder builder = CallHistoryResponse.builder()
                .id(callHistory.getId())
                .aparty(callHistory.getAparty())
                .bparty(callHistory.getBparty())
                .uuid(callHistory.getUuid())
                .sourceIp(callHistory.getSourceIp())
                .createTime(callHistory.getCreateTime())
                .startTime(callHistory.getStartTime())
                .endTime(callHistory.getEndTime())
                .duration(callHistory.getDuration())
                .status(callHistory.getStatus())
                .hangupCause(callHistory.getHangupCause())
                .codec(callHistory.getCodec())
                .idUser(callHistory.getIdUser());

        // Fetch and include user information if user ID exists
        if (callHistory.getIdUser() != null) {
            userRepository.findById(callHistory.getIdUser()).ifPresent(user -> {
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
