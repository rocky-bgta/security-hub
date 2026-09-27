package com.aspire.asat.notification.service;

import com.aspire.asat.common.dto.notification.InAppNotificationDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.enums.ChannelStatus;
import com.aspire.asat.notification.model.InAppNotification;
import com.aspire.asat.notification.repository.InAppNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing in-app notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationService {
    
    private final InAppNotificationRepository repository;
    private final NotificationHistoryService notificationHistoryService;
    
    /**
     * Create a new in-app notification
     */
    public void createNotification(InAppNotificationDto dto) {
        createNotification(dto, null);
    }
    
    /**
     * Create a new in-app notification with logging
     */
    public void createNotification(InAppNotificationDto dto, String logId) {
        InAppNotification notification = mapToEntity(dto);
        notification.setId(UUID.randomUUID().toString());
        notification.setCreatedAt(LocalDateTime.now());
        
        try {
            InAppNotification saved = repository.save(notification);
            log.info("Created in-app notification {} for user {}", saved.getId(), dto.getUserId());
            
            // Update notification log with success status
            if (logId != null) {
                notificationHistoryService.updateChannelStatus(logId, 
                        NotificationChannel.IN_APP,
                        ChannelStatus.SUCCESS, null);
            }
        } catch (Exception e) {
            log.error("Failed to create in-app notification: {}", e.getMessage(), e);
            if (logId != null) {
                notificationHistoryService.updateChannelStatus(logId, 
                        NotificationChannel.IN_APP, 
                        ChannelStatus.FAILED, e.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Get all notifications for a user
     */
    public List<InAppNotification> getUserNotifications(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get notifications for a user with pagination
     */
    public Page<InAppNotification> getUserNotifications(String userId, Pageable pageable) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * Get notifications for a user with pagination and search
     */
    public Page<InAppNotification> getUserNotifications(String userId, String search, Pageable pageable) {
        // Use custom repository for proper offset-based pagination (handles both search and non-search)
        // Extract offset and pageSize from Pageable
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();
        return repository.findByUserIdWithPagination(userId, search, offset, pageSize);
    }
    
    /**
     * Get notifications for a user with pagination and search using offset directly
     */
    public Page<InAppNotification> getUserNotifications(String userId, String search, int offset, int pageSize) {
        return repository.findByUserIdWithPagination(userId, search, offset, pageSize);
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<InAppNotification> getUnreadNotifications(String userId) {
        return repository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get unread notifications for a user with pagination and search
     */
    public Page<InAppNotification> getUnreadNotifications(String userId, String search, Pageable pageable) {
        // Use custom repository for proper offset-based pagination (handles both search and non-search)
        // Extract offset and pageSize from Pageable
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();
        return repository.findUnreadByUserIdWithPagination(userId, search, offset, pageSize);
    }
    
    /**
     * Get unread notifications for a user with pagination and search using offset directly
     */
    public Page<InAppNotification> getUnreadNotifications(String userId, String search, int offset, int pageSize) {
        return repository.findUnreadByUserIdWithPagination(userId, search, offset, pageSize);
    }
    
    /**
     * Get read notifications for a user
     */
    public List<InAppNotification> getReadNotifications(String userId) {
        return repository.findByUserIdAndIsReadTrueOrderByCreatedAtDesc(userId);
    }

    /**
     * Get archived (read) notifications for a user with pagination and search using offset
     */
    public Page<InAppNotification> getArchivedNotifications(String userId, String search, int offset, int pageSize) {
        return repository.findArchivedByUserIdWithPagination(userId, search, offset, pageSize);
    }
    
    /**
     * Get notifications by user and notification type
     */
    public List<InAppNotification> getUserNotificationsByType(String userId, NotificationType notificationType) {
        return repository.findByUserIdAndNotificationTypeOrderByCreatedAtDesc(userId, notificationType);
    }
    
    /**
     * Count unread notifications for a user
     */
    public long getUnreadCount(String userId) {
        return repository.countByUserIdAndIsReadFalse(userId);
    }
    
    /**
     * Get notifications created after a specific date
     */
    public List<InAppNotification> getNotificationsAfter(String userId, LocalDateTime createdAt) {
        return repository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, createdAt);
    }
    
    /**
     * Mark a notification as read
     */
    public void markAsRead(String notificationId) {
        Optional<InAppNotification> notificationOpt = repository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            InAppNotification notification = notificationOpt.get();
            notification.markAsRead();
            repository.save(notification);
            log.info("Marked notification {} as read", notificationId);
        } else {
            log.warn("Notification not found for marking as read: {}", notificationId);
        }
    }
    
    /**
     * Mark a notification as unread
     */
    public void markAsUnread(String notificationId) {
        Optional<InAppNotification> notificationOpt = repository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            InAppNotification notification = notificationOpt.get();
            notification.markAsUnread();
            repository.save(notification);
            log.info("Marked notification {} as unread", notificationId);
        } else {
            log.warn("Notification not found for marking as unread: {}", notificationId);
        }
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public int markAllAsRead(String userId) {
        LocalDateTime readAt = LocalDateTime.now();
        repository.markAllAsReadForUser(userId, readAt);
        
        // Count how many were marked as read
        long unreadCount = repository.countByUserIdAndIsReadFalse(userId);
        log.info("Marked notifications as read for user {}", userId);
        return 0; // MongoDB update doesn't return count, so we return 0
    }
    
    /**
     * Delete a notification
     */
    public void deleteNotification(String notificationId) {
        repository.deleteById(notificationId);
        log.info("Deleted notification {}", notificationId);
    }
    
    /**
     * Delete old notifications (for cleanup)
     */
    public void deleteOldNotifications(LocalDateTime cutoffDate) {
        repository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Deleted notifications older than {}", cutoffDate);
    }
    
    /**
     * Get notification by ID
     */
    public Optional<InAppNotification> getNotification(String notificationId) {
        return repository.findById(notificationId);
    }
    
    /**
     * Map DTO to model
     */
    private InAppNotification mapToEntity(InAppNotificationDto dto) {
        return InAppNotification.builder()
            .userId(dto.getUserId())
            .title(dto.getTitle())
            .message(dto.getMessage())
            .notificationType(dto.getNotificationType())
            .isRead(dto.isRead())
            .metadata(convertMetadataToString(dto.getMetadata()))
            .createdAt(dto.getCreatedAt())
            .readAt(dto.getReadAt())
            .build();
    }
    
    /**
     * Convert metadata map to JSON string
     */
    private String convertMetadataToString(java.util.Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("Error converting metadata to JSON: {}", e.getMessage(), e);
            return null;
        }
    }
}
