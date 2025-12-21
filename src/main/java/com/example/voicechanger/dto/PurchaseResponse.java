package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private Long id;
    private LocalDateTime purchaseDate;
    private BigDecimal purchaseAmount;
    private Long packageId;
    private String packageName;
    private Long duration;
    private String transactionId;
    private String transactionStatus;
    private Long userId;
    private String username;
}
