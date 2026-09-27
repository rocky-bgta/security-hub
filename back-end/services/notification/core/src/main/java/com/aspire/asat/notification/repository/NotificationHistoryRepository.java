package com.aspire.asat.notification.repository;

import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.enums.DeliveryStatus;
import com.aspire.asat.notification.model.NotificationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * MongoDB repository for NotificationHistory document
 * Provides query methods for notification history tracking and analytics
 */
@Repository
public interface NotificationHistoryRepository extends MongoRepository<NotificationHistory, String> {
    
    /**
     * Find notification history for a user
     */
    Page<NotificationHistory> findByUserIdOrderBySentAtDesc(String userId, Pageable pageable);
    
    /**
     * Find notification history by notification type
     */
    Page<NotificationHistory> findByNotificationTypeOrderBySentAtDesc(NotificationType notificationType, Pageable pageable);
    
    /**
     * Find notification history by delivery status
     */
    Page<NotificationHistory> findByDeliveryStatusOrderBySentAtDesc(DeliveryStatus deliveryStatus, Pageable pageable);
    
    /**
     * Find notification history by date range
     */
    Page<NotificationHistory> findBySentAtBetweenOrderBySentAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);
}

