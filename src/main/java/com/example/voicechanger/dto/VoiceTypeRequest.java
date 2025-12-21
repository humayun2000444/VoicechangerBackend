package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceTypeRequest {

    @NotBlank(message = "Voice name is required")
    @Size(max = 100, message = "Voice name must not exceed 100 characters")
    private String voiceName;

    @NotBlank(message = "Code is required")
    @Size(max = 10, message = "Code must not exceed 10 characters")
    private String code;
}
