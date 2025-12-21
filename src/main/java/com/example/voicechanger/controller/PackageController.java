package com.example.voicechanger.controller;

import com.example.voicechanger.dto.PackageRequest;
import com.example.voicechanger.dto.PackageResponse;
import com.example.voicechanger.service.PackageService;
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
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@Slf4j
public class PackageController {

    private final PackageService packageService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPackages() {
        log.info("GET /api/packages - Fetching all packages");
        List<PackageResponse> packages = packageService.getAllPackages();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Packages retrieved successfully");
        response.put("data", packages);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPackageById(@PathVariable Long id) {
        log.info("GET /api/packages/{} - Fetching package", id);
        PackageResponse pkg = packageService.getPackageById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Package retrieved successfully");
        response.put("data", pkg);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPackagesByName(@RequestParam String name) {
        log.info("GET /api/packages/search?name={} - Searching packages", name);
        List<PackageResponse> packages = packageService.searchPackagesByName(name);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Packages retrieved successfully");
        response.put("data", packages);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createPackage(@Valid @RequestBody PackageRequest request) {
        log.info("POST /api/packages - Creating package: {}", request.getPackageName());
        PackageResponse pkg = packageService.createPackage(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Package created successfully");
        response.put("data", pkg);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody PackageRequest request) {
        log.info("PUT /api/packages/{} - Updating package", id);
        PackageResponse pkg = packageService.updatePackage(id, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Package updated successfully");
        response.put("data", pkg);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deletePackage(@PathVariable Long id) {
        log.info("DELETE /api/packages/{} - Deleting package", id);
        packageService.deletePackage(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Package deleted successfully");

        return ResponseEntity.ok(response);
    }
}
