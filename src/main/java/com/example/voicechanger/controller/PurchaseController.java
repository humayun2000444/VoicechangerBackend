package com.example.voicechanger.controller;

import com.example.voicechanger.dto.PurchaseRequest;
import com.example.voicechanger.dto.PurchaseResponse;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Slf4j
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> purchasePackage(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PurchaseRequest request) {
        log.info("POST /api/purchases - User {} purchasing package {}", user.getUsername(), request.getPackageId());
        PurchaseResponse purchase = purchaseService.purchasePackage(user.getId(), request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Package purchased successfully");
        response.put("data", purchase);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyPurchases(@AuthenticationPrincipal User user) {
        log.info("GET /api/purchases/my - Fetching purchases for user {}", user.getUsername());
        List<PurchaseResponse> purchases = purchaseService.getMyPurchases(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Purchases retrieved successfully");
        response.put("data", purchases);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPurchaseById(@PathVariable Long id) {
        log.info("GET /api/purchases/{} - Fetching purchase", id);
        PurchaseResponse purchase = purchaseService.getPurchaseById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Purchase retrieved successfully");
        response.put("data", purchase);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllPurchases() {
        log.info("GET /api/purchases - Fetching all purchases");
        List<PurchaseResponse> purchases = purchaseService.getAllPurchases();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All purchases retrieved successfully");
        response.put("data", purchases);

        return ResponseEntity.ok(response);
    }
}
