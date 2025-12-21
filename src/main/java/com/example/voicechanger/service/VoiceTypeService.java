package com.example.voicechanger.service;

import com.example.voicechanger.dto.VoiceTypeRequest;
import com.example.voicechanger.dto.VoiceTypeResponse;
import com.example.voicechanger.entity.VoiceType;
import com.example.voicechanger.repository.VoiceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceTypeService {

    private final VoiceTypeRepository voiceTypeRepository;

    @Transactional(readOnly = true)
    public List<VoiceTypeResponse> getAllVoiceTypes() {
        log.info("Fetching all voice types");
        return voiceTypeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VoiceTypeResponse getVoiceTypeById(Long id) {
        log.info("Fetching voice type with ID: {}", id);
        VoiceType voiceType = voiceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voice type not found with ID: " + id));
        return convertToResponse(voiceType);
    }

    @Transactional(readOnly = true)
    public VoiceTypeResponse getVoiceTypeByCode(String code) {
        log.info("Fetching voice type with code: {}", code);
        VoiceType voiceType = voiceTypeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voice type not found with code: " + code));
        return convertToResponse(voiceType);
    }

    @Transactional
    public VoiceTypeResponse createVoiceType(VoiceTypeRequest request) {
        log.info("Creating new voice type with code: {}", request.getCode());

        // Check if code already exists
        if (voiceTypeRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Voice type with code " + request.getCode() + " already exists");
        }

        VoiceType voiceType = new VoiceType();
        voiceType.setVoiceName(request.getVoiceName());
        voiceType.setCode(request.getCode());
        voiceType.setCreatedAt(LocalDateTime.now());
        voiceType.setUpdatedAt(LocalDateTime.now());

        VoiceType savedVoiceType = voiceTypeRepository.save(voiceType);
        log.info("Voice type created successfully with ID: {}", savedVoiceType.getId());

        return convertToResponse(savedVoiceType);
    }

    @Transactional
    public VoiceTypeResponse updateVoiceType(Long id, VoiceTypeRequest request) {
        log.info("Updating voice type with ID: {}", id);

        VoiceType voiceType = voiceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voice type not found with ID: " + id));

        // Check if code is being changed and if new code already exists
        if (!voiceType.getCode().equals(request.getCode()) &&
            voiceTypeRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Voice type with code " + request.getCode() + " already exists");
        }

        voiceType.setVoiceName(request.getVoiceName());
        voiceType.setCode(request.getCode());
        voiceType.setUpdatedAt(LocalDateTime.now());

        VoiceType updatedVoiceType = voiceTypeRepository.save(voiceType);
        log.info("Voice type updated successfully with ID: {}", updatedVoiceType.getId());

        return convertToResponse(updatedVoiceType);
    }

    @Transactional
    public void deleteVoiceType(Long id) {
        log.info("Deleting voice type with ID: {}", id);

        if (!voiceTypeRepository.existsById(id)) {
            throw new RuntimeException("Voice type not found with ID: " + id);
        }

        voiceTypeRepository.deleteById(id);
        log.info("Voice type deleted successfully with ID: {}", id);
    }

    private VoiceTypeResponse convertToResponse(VoiceType voiceType) {
        return VoiceTypeResponse.builder()
                .id(voiceType.getId())
                .voiceName(voiceType.getVoiceName())
                .code(voiceType.getCode())
                .createdAt(voiceType.getCreatedAt())
                .updatedAt(voiceType.getUpdatedAt())
                .build();
    }
}
