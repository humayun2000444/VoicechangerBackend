package com.example.voicechanger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoicePurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_user", nullable = false)
    private Long idUser;

    @Column(name = "id_voice_type", nullable = false)
    private Long idVoiceType;

    @Column(name = "id_transaction")
    private Long idTransaction;

    @Column(name = "transaction_method", length = 50)
    private String transactionMethod; // bkash, nagad, rocket

    @Column(name = "tnx_id", length = 100)
    private String tnxId;

    @Column(name = "subscription_type", length = 20)
    private String subscriptionType; // "monthly", "yearly"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "purchase_date", nullable = false, updatable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate; // When the subscription expires

    @Column(nullable = false, length = 20)
    private String status; // "pending", "approved", "rejected"

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_voice_type", referencedColumnName = "id", insertable = false, updatable = false)
    private VoiceType voiceType;

    @PrePersist
    protected void onCreate() {
        purchaseDate = LocalDateTime.now();
        if (status == null || status.trim().isEmpty()) {
            status = "pending"; // Default status is pending, waiting for admin approval
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
