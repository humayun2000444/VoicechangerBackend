package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoicePurchaseResponse {

    private Long id;
    private Long idUser;
    private Long idVoiceType;
    private Long idTransaction;
    private String transactionMethod; // bkash, nagad, rocket
    private String tnxId;
    private String subscriptionType; // "monthly", "yearly"
    private BigDecimal amount;
    private LocalDateTime purchaseDate;
    private LocalDateTime expiryDate; // When the subscription expires
    private String status; // "pending", "approved", "rejected"
    private LocalDateTime updatedAt;
    private VoiceTypeResponse voiceType;
    private UserResponse user;
}
