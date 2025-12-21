package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponse {
    private Long id;
    private String packageName;
    private Long duration; // Duration in seconds
    private Set<VoiceTypeResponse> voiceTypes;
    private LocalDateTime createdDate;
    private LocalDate expireDate;
    private BigDecimal price;
    private BigDecimal vat;
    private BigDecimal totalAmount;
}
