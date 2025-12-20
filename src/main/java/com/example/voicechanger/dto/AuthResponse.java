package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private LocalDateTime createdAt;
    private String message;

    public AuthResponse(String token, String username, String firstName, String lastName, List<String> roles, LocalDateTime createdAt) {
        this.token = token;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.createdAt = createdAt;
    }
}
