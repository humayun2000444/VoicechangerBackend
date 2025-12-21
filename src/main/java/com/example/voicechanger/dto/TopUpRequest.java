package com.example.voicechanger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopUpRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "20.0", message = "Minimum top-up amount is 20 BDT")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    private String transactionMethod;

    @NotBlank(message = "Transaction ID is required")
    private String tnxId;
}
