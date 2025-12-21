package com.example.voicechanger.controller;

import com.example.voicechanger.dto.VoiceTypeRequest;
import com.example.voicechanger.dto.VoiceTypeResponse;
import com.example.voicechanger.service.VoiceTypeService;
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
@RequestMapping("/api/voice-types")
@RequiredArgsConstructor
@Slf4j
public class VoiceTypeController {

    private final VoiceTypeService voiceTypeService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllVoiceTypes() {
        log.info("GET /api/voice-types - Fetching all voice types");
        List<VoiceTypeResponse> voiceTypes = voiceTypeService.getAllVoiceTypes();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice types retrieved successfully");
        response.put("data", voiceTypes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVoiceTypeById(@PathVariable Long id) {
        log.info("GET /api/voice-types/{} - Fetching voice type", id);
        VoiceTypeResponse voiceType = voiceTypeService.getVoiceTypeById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice type retrieved successfully");
        response.put("data", voiceType);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getVoiceTypeByCode(@PathVariable String code) {
        log.info("GET /api/voice-types/code/{} - Fetching voice type", code);
        VoiceTypeResponse voiceType = voiceTypeService.getVoiceTypeByCode(code);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice type retrieved successfully");
        response.put("data", voiceType);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createVoiceType(@Valid @RequestBody VoiceTypeRequest request) {
        log.info("POST /api/voice-types - Creating voice type: {}", request.getCode());
        VoiceTypeResponse voiceType = voiceTypeService.createVoiceType(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice type created successfully");
        response.put("data", voiceType);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateVoiceType(
            @PathVariable Long id,
            @Valid @RequestBody VoiceTypeRequest request) {
        log.info("PUT /api/voice-types/{} - Updating voice type", id);
        VoiceTypeResponse voiceType = voiceTypeService.updateVoiceType(id, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice type updated successfully");
        response.put("data", voiceType);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteVoiceType(@PathVariable Long id) {
        log.info("DELETE /api/voice-types/{} - Deleting voice type", id);
        voiceTypeService.deleteVoiceType(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Voice type deleted successfully");

        return ResponseEntity.ok(response);
    }
}
