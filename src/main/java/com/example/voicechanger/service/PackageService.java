package com.example.voicechanger.service;

import com.example.voicechanger.dto.PackageRequest;
import com.example.voicechanger.dto.PackageResponse;
import com.example.voicechanger.dto.VoiceTypeResponse;
import com.example.voicechanger.entity.Package;
import com.example.voicechanger.entity.VoiceType;
import com.example.voicechanger.repository.PackageRepository;
import com.example.voicechanger.repository.VoiceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private final PackageRepository packageRepository;
    private final VoiceTypeRepository voiceTypeRepository;

    @Transactional(readOnly = true)
    public List<PackageResponse> getAllPackages() {
        log.info("Fetching all packages");
        return packageRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PackageResponse getPackageById(Long id) {
        log.info("Fetching package with ID: {}", id);
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with ID: " + id));
        return convertToResponse(pkg);
    }

    @Transactional(readOnly = true)
    public List<PackageResponse> searchPackagesByName(String name) {
        log.info("Searching packages by name: {}", name);
        return packageRepository.findByPackageNameContainingIgnoreCase(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PackageResponse createPackage(PackageRequest request) {
        log.info("Creating new package: {}", request.getPackageName());

        // Fetch voice types
        Set<VoiceType> voiceTypes = new HashSet<>();
        for (Long voiceTypeId : request.getVoiceTypeIds()) {
            VoiceType voiceType = voiceTypeRepository.findById(voiceTypeId)
                    .orElseThrow(() -> new RuntimeException("Voice type not found with ID: " + voiceTypeId));
            voiceTypes.add(voiceType);
        }

        // Calculate total amount
        BigDecimal totalAmount = request.getPrice().add(request.getVat());

        Package pkg = new Package();
        pkg.setPackageName(request.getPackageName());
        pkg.setDuration(request.getDuration());
        pkg.setVoiceTypes(voiceTypes);
        pkg.setCreatedDate(LocalDateTime.now());
        pkg.setExpireDate(request.getExpireDate());
        pkg.setPrice(request.getPrice());
        pkg.setVat(request.getVat());
        pkg.setTotalAmount(totalAmount);

        Package savedPackage = packageRepository.save(pkg);
        log.info("Package created successfully with ID: {}", savedPackage.getId());

        return convertToResponse(savedPackage);
    }

    @Transactional
    public PackageResponse updatePackage(Long id, PackageRequest request) {
        log.info("Updating package with ID: {}", id);

        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with ID: " + id));

        // Fetch voice types
        Set<VoiceType> voiceTypes = new HashSet<>();
        for (Long voiceTypeId : request.getVoiceTypeIds()) {
            VoiceType voiceType = voiceTypeRepository.findById(voiceTypeId)
                    .orElseThrow(() -> new RuntimeException("Voice type not found with ID: " + voiceTypeId));
            voiceTypes.add(voiceType);
        }

        // Calculate total amount
        BigDecimal totalAmount = request.getPrice().add(request.getVat());

        pkg.setPackageName(request.getPackageName());
        pkg.setDuration(request.getDuration());
        pkg.setVoiceTypes(voiceTypes);
        pkg.setExpireDate(request.getExpireDate());
        pkg.setPrice(request.getPrice());
        pkg.setVat(request.getVat());
        pkg.setTotalAmount(totalAmount);

        Package updatedPackage = packageRepository.save(pkg);
        log.info("Package updated successfully with ID: {}", updatedPackage.getId());

        return convertToResponse(updatedPackage);
    }

    @Transactional
    public void deletePackage(Long id) {
        log.info("Deleting package with ID: {}", id);

        if (!packageRepository.existsById(id)) {
            throw new RuntimeException("Package not found with ID: " + id);
        }

        packageRepository.deleteById(id);
        log.info("Package deleted successfully with ID: {}", id);
    }

    private PackageResponse convertToResponse(Package pkg) {
        Set<VoiceTypeResponse> voiceTypeResponses = pkg.getVoiceTypes().stream()
                .map(vt -> VoiceTypeResponse.builder()
                        .id(vt.getId())
                        .voiceName(vt.getVoiceName())
                        .code(vt.getCode())
                        .createdAt(vt.getCreatedAt())
                        .updatedAt(vt.getUpdatedAt())
                        .build())
                .collect(Collectors.toSet());

        return PackageResponse.builder()
                .id(pkg.getId())
                .packageName(pkg.getPackageName())
                .duration(pkg.getDuration())
                .voiceTypes(voiceTypeResponses)
                .createdDate(pkg.getCreatedDate())
                .expireDate(pkg.getExpireDate())
                .price(pkg.getPrice())
                .vat(pkg.getVat())
                .totalAmount(pkg.getTotalAmount())
                .build();
    }
}
