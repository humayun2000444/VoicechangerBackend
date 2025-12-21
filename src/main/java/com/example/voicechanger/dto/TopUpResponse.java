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
public class TopUpResponse {

    private Long id;
    private Long idUser;
    private String transactionMethod;
    private BigDecimal amount;
    private String tnxId;
    private LocalDateTime date;
    private String status;
    private LocalDateTime updatedAt;
    private Long durationInSeconds;
    private UserResponse user;
}
