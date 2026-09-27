package com.aspire.asat.notification.dto;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for notification statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatisticsDto {
    
    private long totalSent;
    private long totalSuccess;
    private long totalFailed;
    private long totalPartial;
    private long totalPending;
    
    private double successRate;
    private double failureRate;
    
    private Map<NotificationType, TypeStatistics> byType;
    private Map<NotificationChannel, ChannelStatistics> byChannel;
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    /**
     * Statistics for a specific notification type
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStatistics {
        private NotificationType notificationType;
        private long totalSent;
        private long totalSuccess;
        private long totalFailed;
        private long totalPartial;
        private double successRate;
    }
    
    /**
     * Statistics for a specific channel
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelStatistics {
        private NotificationChannel channel;
        private long totalSent;
        private long totalSuccess;
        private long totalFailed;
        private long totalSkipped;
        private double successRate;
    }
}

