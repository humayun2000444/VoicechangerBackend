package com.example.voicechanger.controller;

import com.example.voicechanger.dto.BalanceResponse;
import com.example.voicechanger.entity.User;
import com.example.voicechanger.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
@Slf4j
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyBalance(@AuthenticationPrincipal User user) {
        log.info("GET /api/balance/my - Fetching balance for user {}", user.getUsername());
        BalanceResponse balance = balanceService.getBalanceByUserId(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Balance retrieved successfully");
        response.put("data", balance);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserBalance(@PathVariable Long userId) {
        log.info("GET /api/balance/user/{} - Fetching balance for user ID", userId);
        BalanceResponse balance = balanceService.getBalanceByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Balance retrieved successfully");
        response.put("data", balance);

        return ResponseEntity.ok(response);
    }
}
