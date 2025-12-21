package com.example.voicechanger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "call_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aparty", nullable = false, length = 50)
    private String aparty;

    @Column(name = "bparty", length = 50)
    private String bparty;

    @Column(name = "uuid", nullable = false, unique = true, length = 100)
    private String uuid;

    @Column(name = "source_ip", length = 50)
    private String sourceIp;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long duration; // duration in seconds

    @Column(name = "status", length = 20)
    private String status; // RESERVED, ANSWERED, COMPLETED, REJECTED, FAILED

    @Column(name = "hangup_cause", length = 50)
    private String hangupCause; // NORMAL_CLEARING, USER_BUSY, NO_ANSWER, etc.

    @Column(name = "codec", length = 50)
    private String codec; // PCMU, PCMA, OPUS, etc.

    @Column(name = "id_user")
    private Long idUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;
}
