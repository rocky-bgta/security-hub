package com.aspire.asat.notification.repository;

import com.aspire.asat.notification.model.ClientNotificationSettings;
import com.aspire.asat.common.enums.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for ClientNotificationSettings document
 * Handles client-specific notification preferences
 */
@Repository
public interface ClientNotificationSettingsRepository extends MongoRepository<ClientNotificationSettings, String> {
    
    /**
     * Find notification settings by client admin ID and notification type
     */
    Optional<ClientNotificationSettings> findByClientAdminIdAndNotificationType(String clientAdminId, NotificationType notificationType);
    
    /**
     * Check if a notification setting exists for a client and type
     */
    boolean existsByClientAdminIdAndNotificationType(String clientAdminId, NotificationType notificationType);
    
    /**
     * Find all notification settings for a specific client
     */
    List<ClientNotificationSettings> findByClientAdminId(String clientAdminId);
    
    /**
     * Find all notification settings for a specific client with pagination
     */
    Page<ClientNotificationSettings> findByClientAdminId(String clientAdminId, Pageable pageable);
    
    /**
     * Find all enabled notification settings for a specific client
     */
    List<ClientNotificationSettings> findByClientAdminIdAndEnabledTrue(String clientAdminId);
    
    /**
     * Delete all settings for a specific client
     */
    void deleteByClientAdminId(String clientAdminId);
    
    /**
     * Delete specific notification setting for a client
     */
    void deleteByClientAdminIdAndNotificationType(String clientAdminId, NotificationType notificationType);
}

