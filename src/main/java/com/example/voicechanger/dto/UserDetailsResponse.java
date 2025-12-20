package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {
    private Long idUserDetails;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String email;
    private String profilePhoto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
