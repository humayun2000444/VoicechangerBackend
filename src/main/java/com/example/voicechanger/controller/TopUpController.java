package com.example.voicechanger.controller;

import com.example.voicechanger.dto.TopUpRequest;
import com.example.voicechanger.dto.TopUpResponse;
import com.example.voicechanger.service.TopUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/topup")
@RequiredArgsConstructor
@Slf4j
public class TopUpController {

    private final TopUpService topUpService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> createTopUpRequest(@Valid @RequestBody TopUpRequest request) {
        log.info("POST /api/topup - Creating top-up request with amount: {} BDT", request.getAmount());
        TopUpResponse topUpResponse = topUpService.createTopUpRequest(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Top-up request created successfully. Waiting for admin approval.");
        response.put("data", topUpResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyTopUpRequests() {
        log.info("GET /api/topup/my - Fetching top-up requests for authenticated user");
        List<TopUpResponse> topUpRequests = topUpService.getMyTopUpRequests();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Top-up requests retrieved successfully");
        response.put("data", topUpRequests);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllTopUpRequests() {
        log.info("GET /api/topup - Fetching all top-up requests");
        List<TopUpResponse> topUpRequests = topUpService.getAllTopUpRequests();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All top-up requests retrieved successfully");
        response.put("data", topUpRequests);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingTopUpRequests() {
        log.info("GET /api/topup/pending - Fetching pending top-up requests");
        List<TopUpResponse> topUpRequests = topUpService.getPendingTopUpRequests();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Pending top-up requests retrieved successfully");
        response.put("data", topUpRequests);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTopUpRequestById(@PathVariable Long id) {
        log.info("GET /api/topup/{} - Fetching top-up request", id);
        TopUpResponse topUpRequest = topUpService.getTopUpRequestById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Top-up request retrieved successfully");
        response.put("data", topUpRequest);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveTopUpRequest(@PathVariable Long id) {
        log.info("PATCH /api/topup/{}/approve - Approving top-up request", id);
        TopUpResponse topUpRequest = topUpService.approveTopUpRequest(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Top-up request approved successfully. Balance added to user.");
        response.put("data", topUpRequest);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectTopUpRequest(@PathVariable Long id) {
        log.info("PATCH /api/topup/{}/reject - Rejecting top-up request", id);
        TopUpResponse topUpRequest = topUpService.rejectTopUpRequest(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Top-up request rejected");
        response.put("data", topUpRequest);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTopUpRequest(@PathVariable Long id) {
        log.info("DELETE /api/topup/{} - Deleting top-up request", id);
        topUpService.deleteTopUpRequest(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Top-up request deleted successfully");

        return ResponseEntity.ok(response);
    }
}
