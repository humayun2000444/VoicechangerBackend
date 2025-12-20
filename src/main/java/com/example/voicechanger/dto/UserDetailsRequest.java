package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsRequest {
    private String dateOfBirth;  // Format: yyyy-MM-dd
    private String gender;
    private String address;
    private String email;
    private String profilePhoto;
}
