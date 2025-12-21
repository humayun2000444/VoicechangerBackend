package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceUserMappingResponse {

    private Long id;
    private Long idUser;
    private Long idVoiceType;
    private Boolean isPurchased;
    private LocalDateTime assignedAt;
    private LocalDateTime trialExpiryDate; // null = no trial (permanent), non-null = trial expires at this date
    private LocalDateTime expiryDate; // null = permanent access, non-null = subscription expires at this date
    private VoiceTypeResponse voiceType;
}
