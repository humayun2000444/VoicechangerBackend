package com.example.voicechanger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private Long id;
    private Long purchaseAmount; // Total purchased duration in seconds
    private Long lastUsedAmount; // Last used duration in seconds
    private Long totalUsedAmount; // Total used duration in seconds
    private Long remainAmount; // Remaining duration in seconds
    private Long userId;
    private String username;

    // Helper methods to convert seconds to human-readable format
    public String getRemainAmountFormatted() {
        return formatDuration(remainAmount);
    }

    public String getTotalUsedAmountFormatted() {
        return formatDuration(totalUsedAmount);
    }

    private String formatDuration(Long seconds) {
        if (seconds == null || seconds == 0) {
            return "0 seconds";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d hours %d minutes %d seconds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d minutes %d seconds", minutes, secs);
        } else {
            return String.format("%d seconds", secs);
        }
    }
}
