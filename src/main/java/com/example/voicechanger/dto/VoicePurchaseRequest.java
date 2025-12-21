package com.example.voicechanger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoicePurchaseRequest {

    @NotNull(message = "Voice type ID is required")
    private Long idVoiceType;

    @NotBlank(message = "Payment method is required")
    private String transactionMethod; // bkash, nagad, rocket

    @NotBlank(message = "Transaction ID is required")
    private String tnxId; // Payment transaction ID from mobile banking

    @NotBlank(message = "Subscription type is required")
    private String subscriptionType; // "monthly" or "yearly"
}
