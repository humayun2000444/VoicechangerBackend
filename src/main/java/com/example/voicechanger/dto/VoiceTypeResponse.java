package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceTypeResponse {
    private Long id;
    private String voiceName;
    private String code;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
