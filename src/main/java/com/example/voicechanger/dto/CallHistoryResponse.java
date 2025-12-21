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
public class CallHistoryResponse {
    private Long id;
    private String aparty;
    private String bparty;
    private String uuid;
    private String sourceIp;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String status;
    private String hangupCause;
    private String codec;
    private Long idUser;
    private UserResponse user;
}
