package com.aspire.asat.notification.service;

import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.dto.NotificationStatisticsDto;
import com.aspire.asat.notification.enums.ChannelStatus;
import com.aspire.asat.notification.enums.DeliveryStatus;
import com.aspire.asat.notification.model.NotificationHistory;
import com.aspire.asat.notification.repository.NotificationHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing notification history and logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationHistoryService {
    
    private final NotificationHistoryRepository repository;
    private final ObjectMapper objectMapper;
    
    /**
     * Create initial notification log entry asynchronously
     * The logId is generated in the parent method (controller) and passed here
     */
    @Async
    @Transactional
    public void createNotificationLog(NotificationRequestDto request, String logId) {
        if (logId == null || logId.trim().isEmpty()) {
            log.warn("LogId is null or empty, skipping log creation");
            return;
        }
        try {
            NotificationHistory history = NotificationHistory.builder()
                    .id(logId)
                    .notificationType(request.getNotificationType())
                    .userId(request.getUserId())
                    .recipientEmail(request.getTo())
                    .channels(request.getChannels())
                    .subject(request.getSubject())
                    .templateId(request.getTemplateId())
                    .templateModel(convertToJson(request.getTemplateModel()))
                    .clientAdminId(request.getClientAdminId())
                    .priority(request.getPriority())
                    .requestMetadata(convertToJson(request.getMetadata()))
                    .deliveryStatus(DeliveryStatus.PENDING)
                    .sentAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // Initialize channel statuses as PENDING
            Map<NotificationChannel, ChannelStatus> channelStatuses = new HashMap<>();
            for (NotificationChannel channel : request.getChannels()) {
                channelStatuses.put(channel, ChannelStatus.PENDING);
            }
            history.setChannelStatuses(channelStatuses);
            history.updateDeliveryStatus();
            
            repository.save(history);
            log.debug("Created notification log entry asynchronously: {}", logId);
            
        } catch (Exception e) {
            log.error("Failed to create notification log: {}", e.getMessage(), e);
            // Don't throw exception - logging failure shouldn't break notification delivery
        }
    }
    
    /**
     * Update channel status
     */
    @Async
    @Transactional
    public void updateChannelStatus(String logId, NotificationChannel channel, ChannelStatus status, String errorMessage) {
        if (logId == null || logId.trim().isEmpty()) {
            // Silently skip if logId is null - logging is optional
            return;
        }
        try {
            Optional<NotificationHistory> historyOpt = repository.findById(logId);
            if (historyOpt.isEmpty()) {
                log.warn("Notification log not found: {}", logId);
                return;
            }
            
            NotificationHistory history = historyOpt.get();
            history.updateChannelStatus(channel, status);
            
            if (errorMessage != null && !errorMessage.isEmpty()) {
                history.setErrorMessage(errorMessage);
            }
            
            history.setUpdatedAt(LocalDateTime.now());
            repository.save(history);
            log.debug("Updated channel status for log {}: {} -> {}", logId, channel, status);
            
        } catch (Exception e) {
            log.error("Failed to update channel status for log {}: {}", logId, e.getMessage(), e);
            // Don't throw - logging failure shouldn't break notification delivery
        }
    }
    
    /**
     * Finalize notification log
     */
    @Async
    @Transactional
    public void finalizeNotificationLog(String logId, DeliveryStatus status) {
        if (logId == null || logId.trim().isEmpty()) {
            // Silently skip if logId is null - logging is optional
            return;
        }
        try {
            Optional<NotificationHistory> historyOpt = repository.findById(logId);
            if (historyOpt.isEmpty()) {
                log.warn("Notification log not found: {}", logId);
                return;
            }
            
            NotificationHistory history = historyOpt.get();
            history.setDeliveryStatus(status);
            history.markCompleted();
            history.setUpdatedAt(LocalDateTime.now());
            
            repository.save(history);
            log.debug("Finalized notification log: {} with status: {}", logId, status);
            
        } catch (Exception e) {
            log.error("Failed to finalize notification log {}: {}", logId, e.getMessage(), e);
            // Don't throw - logging failure shouldn't break notification delivery
        }
    }
    
    /**
     * Get notification history by ID
     */
    public Optional<NotificationHistory> getNotificationHistory(String logId) {
        return repository.findById(logId);
    }
    
    /**
     * Get user notification history
     */
    public Page<NotificationHistory> getUserNotificationHistory(String userId, Pageable pageable) {
        return repository.findByUserIdOrderBySentAtDesc(userId, pageable);
    }
    
    /**
     * Get notification history by type
     */
    public Page<NotificationHistory> getNotificationHistoryByType(NotificationType type, Pageable pageable) {
        return repository.findByNotificationTypeOrderBySentAtDesc(type, pageable);
    }
    
    /**
     * Get notification history by status
     */
    public Page<NotificationHistory> getNotificationHistoryByStatus(DeliveryStatus status, Pageable pageable) {
        return repository.findByDeliveryStatusOrderBySentAtDesc(status, pageable);
    }
    
    /**
     * Get notification history by date range
     */
    public Page<NotificationHistory> getNotificationHistoryByDateRange(
            LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return repository.findBySentAtBetweenOrderBySentAtDesc(start, end, pageable);
    }
    
    /**
     * Get notification statistics
     */
    public NotificationStatisticsDto getNotificationStatistics(LocalDateTime start, LocalDateTime end) {
        try {
            // Get all notifications in the period
            List<NotificationHistory> allHistory = repository.findBySentAtBetweenOrderBySentAtDesc(
                    start, end, Pageable.unpaged()).getContent();
            
            long totalSent = allHistory.size();
            long totalSuccess = allHistory.stream()
                    .filter(h -> h.getDeliveryStatus() == DeliveryStatus.SUCCESS)
                    .count();
            long totalFailed = allHistory.stream()
                    .filter(h -> h.getDeliveryStatus() == DeliveryStatus.FAILED)
                    .count();
            long totalPartial = allHistory.stream()
                    .filter(h -> h.getDeliveryStatus() == DeliveryStatus.PARTIAL)
                    .count();
            long totalPending = allHistory.stream()
                    .filter(h -> h.getDeliveryStatus() == DeliveryStatus.PENDING)
                    .count();
            
            double successRate = totalSent > 0 ? (double) totalSuccess / totalSent * 100 : 0.0;
            double failureRate = totalSent > 0 ? (double) totalFailed / totalSent * 100 : 0.0;
            
            // Statistics by type
            Map<NotificationType, NotificationStatisticsDto.TypeStatistics> byType = allHistory.stream()
                    .collect(Collectors.groupingBy(
                            NotificationHistory::getNotificationType,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> {
                                        long sent = list.size();
                                        long success = list.stream()
                                                .filter(h -> h.getDeliveryStatus() == DeliveryStatus.SUCCESS)
                                                .count();
                                        long failed = list.stream()
                                                .filter(h -> h.getDeliveryStatus() == DeliveryStatus.FAILED)
                                                .count();
                                        long partial = list.stream()
                                                .filter(h -> h.getDeliveryStatus() == DeliveryStatus.PARTIAL)
                                                .count();
                                        
                                        return NotificationStatisticsDto.TypeStatistics.builder()
                                                .notificationType(list.get(0).getNotificationType())
                                                .totalSent(sent)
                                                .totalSuccess(success)
                                                .totalFailed(failed)
                                                .totalPartial(partial)
                                                .successRate(sent > 0 ? (double) success / sent * 100 : 0.0)
                                                .build();
                                    }
                            )
                    ));
            
            // Statistics by channel
            Map<NotificationChannel, NotificationStatisticsDto.ChannelStatistics> byChannel = new HashMap<>();
            for (NotificationHistory history : allHistory) {
                if (history.getChannelStatuses() != null) {
                    for (Map.Entry<NotificationChannel, ChannelStatus> entry : history.getChannelStatuses().entrySet()) {
                        NotificationChannel channel = entry.getKey();
                        ChannelStatus status = entry.getValue();
                        
                        NotificationStatisticsDto.ChannelStatistics channelStats = byChannel.computeIfAbsent(
                                channel,
                                k -> NotificationStatisticsDto.ChannelStatistics.builder()
                                        .channel(channel)
                                        .totalSent(0)
                                        .totalSuccess(0)
                                        .totalFailed(0)
                                        .totalSkipped(0)
                                        .build()
                        );
                        
                        channelStats.setTotalSent(channelStats.getTotalSent() + 1);
                        if (status == ChannelStatus.SUCCESS) {
                            channelStats.setTotalSuccess(channelStats.getTotalSuccess() + 1);
                        } else if (status == ChannelStatus.FAILED) {
                            channelStats.setTotalFailed(channelStats.getTotalFailed() + 1);
                        } else if (status == ChannelStatus.SKIPPED) {
                            channelStats.setTotalSkipped(channelStats.getTotalSkipped() + 1);
                        }
                    }
                }
            }
            
            // Calculate success rates for channels
            for (NotificationStatisticsDto.ChannelStatistics channelStats : byChannel.values()) {
                double rate = channelStats.getTotalSent() > 0
                        ? (double) channelStats.getTotalSuccess() / channelStats.getTotalSent() * 100
                        : 0.0;
                channelStats.setSuccessRate(rate);
            }
            
            return NotificationStatisticsDto.builder()
                    .totalSent(totalSent)
                    .totalSuccess(totalSuccess)
                    .totalFailed(totalFailed)
                    .totalPartial(totalPartial)
                    .totalPending(totalPending)
                    .successRate(successRate)
                    .failureRate(failureRate)
                    .byType(byType)
                    .byChannel(byChannel)
                    .periodStart(start)
                    .periodEnd(end)
                    .build();
                    
        } catch (Exception e) {
            log.error("Failed to get notification statistics: {}", e.getMessage(), e);
            return NotificationStatisticsDto.builder()
                    .totalSent(0)
                    .totalSuccess(0)
                    .totalFailed(0)
                    .totalPartial(0)
                    .totalPending(0)
                    .successRate(0.0)
                    .failureRate(0.0)
                    .byType(new HashMap<>())
                    .byChannel(new HashMap<>())
                    .periodStart(start)
                    .periodEnd(end)
                    .build();
        }
    }
    
    /**
     * Convert object to JSON string
     */
    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return null;
        }
    }
}

