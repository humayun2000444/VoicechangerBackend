package com.example.voicechanger.controller;

import com.example.voicechanger.dto.CallHistoryResponse;
import com.example.voicechanger.service.CallHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/call-history")
@RequiredArgsConstructor
@Slf4j
public class CallHistoryController {

    private final CallHistoryService callHistoryService;

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyCallHistory() {
        log.info("GET /api/call-history/my - Fetching call history for authenticated user");
        List<CallHistoryResponse> callHistory = callHistoryService.getMyCallHistory();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history retrieved successfully");
        response.put("data", callHistory);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllCallHistory() {
        log.info("GET /api/call-history - Fetching all call history");
        List<CallHistoryResponse> callHistory = callHistoryService.getAllCallHistory();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history retrieved successfully");
        response.put("data", callHistory);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCallHistoryById(@PathVariable Long id) {
        log.info("GET /api/call-history/{} - Fetching call history", id);
        CallHistoryResponse callHistory = callHistoryService.getCallHistoryById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history retrieved successfully");
        response.put("data", callHistory);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/uuid/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCallHistoryByUuid(@PathVariable String uuid) {
        log.info("GET /api/call-history/uuid/{} - Fetching call history", uuid);
        CallHistoryResponse callHistory = callHistoryService.getCallHistoryByUuid(uuid);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history retrieved successfully");
        response.put("data", callHistory);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCallHistoryByUserId(@PathVariable Long userId) {
        log.info("GET /api/call-history/user/{} - Fetching call history for user", userId);
        List<CallHistoryResponse> callHistory = callHistoryService.getCallHistoryByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history retrieved successfully");
        response.put("data", callHistory);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/aparty/{aparty}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCallHistoryByAparty(@PathVariable String aparty) {
        log.info("GET /api/call-history/aparty/{} - Fetching call history", aparty);
        List<CallHistoryResponse> callHistory = callHistoryService.getCallHistoryByAparty(aparty);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history retrieved successfully");
        response.put("data", callHistory);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCallHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/call-history/date-range - Fetching call history between {} and {}", start, end);
        List<CallHistoryResponse> callHistory = callHistoryService.getCallHistoryByDateRange(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history retrieved successfully");
        response.put("data", callHistory);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCallHistoryByStatus(@PathVariable String status) {
        log.info("GET /api/call-history/status/{} - Fetching call history", status);
        List<CallHistoryResponse> callHistory = callHistoryService.getCallHistoryByStatus(status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history retrieved successfully");
        response.put("data", callHistory);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCallHistory(@PathVariable Long id) {
        log.info("DELETE /api/call-history/{} - Deleting call history", id);
        callHistoryService.deleteCallHistory(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Call history deleted successfully");

        return ResponseEntity.ok(response);
    }
}
