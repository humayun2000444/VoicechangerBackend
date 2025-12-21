package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

    @NotNull(message = "Package ID is required")
    private Long packageId;

    @NotBlank(message = "Transaction method is required")
    private String transactionMethod;

    @NotBlank(message = "Transaction ID is required")
    private String tnxId;
}
