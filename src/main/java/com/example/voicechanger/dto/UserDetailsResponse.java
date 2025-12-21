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
    private Long idUser;
    private UserResponse user;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String email;
    private String profilePhoto;
    private Long selectedVoiceTypeId; // User's selected default voice type for calls
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
