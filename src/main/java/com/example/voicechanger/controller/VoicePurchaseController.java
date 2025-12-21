package com.example.voicechanger.controller;

import com.example.voicechanger.dto.VoicePurchaseRequest;
import com.example.voicechanger.dto.VoicePurchaseResponse;
import com.example.voicechanger.dto.VoiceTypeResponse;
import com.example.voicechanger.dto.VoiceUserMappingResponse;
import com.example.voicechanger.service.VoicePurchaseService;
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
@RequestMapping("/api/voice-purchase")
@RequiredArgsConstructor
@Slf4j
public class VoicePurchaseController {

    private final VoicePurchaseService voicePurchaseService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> purchaseVoiceType(@Valid @RequestBody VoicePurchaseRequest request) {
        log.info("POST /api/voice-purchase - Purchasing voice type ID: {}", request.getIdVoiceType());
        VoicePurchaseResponse purchase = voicePurchaseService.purchaseVoiceType(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice type purchased successfully");
        response.put("data", purchase);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyPurchases() {
        log.info("GET /api/voice-purchase/my - Fetching purchases for authenticated user");
        List<VoicePurchaseResponse> purchases = voicePurchaseService.getMyPurchases();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice purchases retrieved successfully");
        response.put("data", purchases);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllPurchases() {
        log.info("GET /api/voice-purchase - Fetching all purchases");
        List<VoicePurchaseResponse> purchases = voicePurchaseService.getAllPurchases();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All voice purchases retrieved successfully");
        response.put("data", purchases);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/voice-type/{voiceTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPurchasesByVoiceType(@PathVariable Long voiceTypeId) {
        log.info("GET /api/voice-purchase/voice-type/{} - Fetching purchases for voice type", voiceTypeId);
        List<VoicePurchaseResponse> purchases = voicePurchaseService.getPurchasesByVoiceType(voiceTypeId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Purchases retrieved successfully");
        response.put("data", purchases);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-voice-types")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyVoiceTypes() {
        log.info("GET /api/voice-purchase/my-voice-types - Fetching user's available voice types");
        List<VoiceUserMappingResponse> voiceTypes = voicePurchaseService.getMyVoiceTypes();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Your voice types retrieved successfully");
        response.put("data", voiceTypes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-active-voice-types")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyActiveVoiceTypes() {
        log.info("GET /api/voice-purchase/my-active-voice-types - Fetching user's active (non-expired) voice types");
        List<VoiceUserMappingResponse> voiceTypes = voicePurchaseService.getMyActiveVoiceTypes();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Your active voice types retrieved successfully");
        response.put("data", voiceTypes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAvailableVoiceTypes() {
        log.info("GET /api/voice-purchase/available - Fetching all voice types with access info");
        List<VoiceTypeResponse> voiceTypes = voicePurchaseService.getAllVoiceTypesWithAccess();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Available voice types retrieved successfully");
        response.put("data", voiceTypes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingPurchases() {
        log.info("GET /api/voice-purchase/pending - Fetching all pending purchase requests");
        List<VoicePurchaseResponse> purchases = voicePurchaseService.getPendingPurchases();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Pending voice purchase requests retrieved successfully");
        response.put("data", purchases);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approvePurchase(@PathVariable Long id) {
        log.info("PUT /api/voice-purchase/{}/approve - Approving purchase request", id);
        VoicePurchaseResponse purchase = voicePurchaseService.approvePurchase(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice purchase request approved successfully");
        response.put("data", purchase);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectPurchase(@PathVariable Long id) {
        log.info("PUT /api/voice-purchase/{}/reject - Rejecting purchase request", id);
        VoicePurchaseResponse purchase = voicePurchaseService.rejectPurchase(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice purchase request rejected successfully");
        response.put("data", purchase);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deletePurchase(@PathVariable Long id) {
        log.info("DELETE /api/voice-purchase/{} - Deleting purchase", id);
        voicePurchaseService.deletePurchase(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice purchase deleted successfully");

        return ResponseEntity.ok(response);
    }
}
