package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    @NotBlank(message = "Package name is required")
    @Size(max = 200, message = "Package name must not exceed 200 characters")
    private String packageName;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    private Long duration; // Duration in seconds

    @NotEmpty(message = "At least one voice type is required")
    private Set<Long> voiceTypeIds;

    @NotNull(message = "Expire date is required")
    @Future(message = "Expire date must be in the future")
    private LocalDate expireDate;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "VAT is required")
    @DecimalMin(value = "0.0", message = "VAT must be 0 or greater")
    @Digits(integer = 10, fraction = 2, message = "VAT must have at most 10 integer digits and 2 decimal places")
    private BigDecimal vat;
}
