package com.example.voicechanger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "voice_mapping_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceMappingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_mapping_id", nullable = false)
    private Long originalMappingId;

    @Column(name = "id_user", nullable = false)
    private Long idUser;

    @Column(name = "id_voice_type", nullable = false)
    private Long idVoiceType;

    @Column(name = "is_purchased", nullable = false)
    private Boolean isPurchased;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "trial_expiry_date")
    private LocalDateTime trialExpiryDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "expiry_reason", length = 50)
    private String expiryReason; // TRIAL_EXPIRED or SUBSCRIPTION_EXPIRED or BOTH_EXPIRED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_voice_type", referencedColumnName = "id", insertable = false, updatable = false)
    private VoiceType voiceType;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiredAt == null) {
            expiredAt = LocalDateTime.now();
        }
    }

    /**
     * Create a history record from an expired VoiceUserMapping
     */
    public static VoiceMappingHistory fromExpiredMapping(VoiceUserMapping mapping, String expiryReason) {
        return VoiceMappingHistory.builder()
                .originalMappingId(mapping.getId())
                .idUser(mapping.getIdUser())
                .idVoiceType(mapping.getIdVoiceType())
                .isPurchased(mapping.getIsPurchased())
                .assignedAt(mapping.getAssignedAt())
                .trialExpiryDate(mapping.getTrialExpiryDate())
                .expiryDate(mapping.getExpiryDate())
                .isDefault(mapping.getIsDefault())
                .expiredAt(LocalDateTime.now())
                .expiryReason(expiryReason)
                .build();
    }
}
