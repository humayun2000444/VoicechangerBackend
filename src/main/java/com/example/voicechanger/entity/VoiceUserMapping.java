package com.example.voicechanger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "voice_user_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceUserMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_user", nullable = false)
    private Long idUser;

    @Column(name = "id_voice_type", nullable = false)
    private Long idVoiceType;

    @Column(name = "is_purchased", nullable = false)
    private Boolean isPurchased = false; // false = free (auto-assigned), true = purchased

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "trial_expiry_date")
    private LocalDateTime trialExpiryDate; // null = no trial (permanent), non-null = trial expires at this date

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate; // null = permanent access, non-null = subscription expires at this date

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_voice_type", referencedColumnName = "id", insertable = false, updatable = false)
    private VoiceType voiceType;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
        if (isPurchased == null) {
            isPurchased = false;
        }
    }
}
