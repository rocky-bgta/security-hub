package com.aspire.asat.notification.repository;

import com.aspire.asat.notification.model.NotificationSettings;
import com.aspire.asat.common.enums.notification.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for NotificationSettings document
 */
@Repository
public interface NotificationSettingsRepository extends MongoRepository<NotificationSettings, String> {
    
    /**
     * Find notification settings by notification type
     */
    Optional<NotificationSettings> findByNotificationType(NotificationType notificationType);

    
    /**
     * Find all enabled notification settings
     */
    List<NotificationSettings> findByEnabledTrue();

    
    /**
     * Check if a notification type is enabled
     */
    @Query("{ 'notificationType': ?0, 'enabled': true }")
    Optional<Boolean> isNotificationTypeEnabled(NotificationType notificationType);
    
    /**
     * Find all notification settings for priority types
     */
    @Query("{ 'notificationType': { $in: ?0 } }")
    List<NotificationSettings> findByPriorityTypes(List<NotificationType> priorityTypes);

    /**
     * Find all notification settings ordered by notification type ascending
     */
    List<NotificationSettings> findAllByOrderByCreatedAtAsc();
}
