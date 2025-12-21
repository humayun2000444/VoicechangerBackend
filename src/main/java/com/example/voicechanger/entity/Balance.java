package com.example.voicechanger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_amount", nullable = false)
    private Long purchaseAmount; // Duration in seconds

    @Column(name = "last_used_amount", nullable = false)
    private Long lastUsedAmount; // Duration in seconds

    @Column(name = "total_used_amount", nullable = false)
    private Long totalUsedAmount; // Duration in seconds

    @Column(name = "remain_amount", nullable = false)
    private Long remainAmount; // Duration in seconds

    @Column(name = "id_user", nullable = false, unique = true)
    private Long idUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", insertable = false, updatable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
