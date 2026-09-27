package com.aspire.asat.notification.repository;

import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for NotificationTemplate document
 */
@Repository
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String>,
        NotificationTemplateRepositoryCustom {
    
    /**
     * Find all templates by notification type and channel
     */
    List<NotificationTemplate> findAllByNotificationTypeAndChannel(NotificationType notificationType, NotificationChannel channel);

    /**
     * Same as {@link #findAllByNotificationTypeAndChannel} but scoped to a single recipient
     * role (or {@code null} for the role-agnostic template), so activating a role-specific
     * template only deactivates other templates for that same role, not every role.
     */
    List<NotificationTemplate> findAllByNotificationTypeAndChannelAndRecipientRole(
        NotificationType notificationType, NotificationChannel channel, NotificationRecipientRole recipientRole);
    
    /**
     * Find template by notification type and channel
     */
    Optional<NotificationTemplate> findByNotificationTypeAndChannel(NotificationType notificationType, NotificationChannel channel);
    
    /**
     * Find an active template by notification type and channel
     */
    Optional<NotificationTemplate> findByNotificationTypeAndChannelAndIsActiveTrue(
        NotificationType notificationType, NotificationChannel channel);

    /**
     * Find an active, role-specific template by notification type, channel and recipient role.
     * Used by role-aware template lookup before falling back to the role-agnostic template.
     */
    Optional<NotificationTemplate> findByNotificationTypeAndChannelAndRecipientRoleAndIsActiveTrue(
        NotificationType notificationType, NotificationChannel channel, NotificationRecipientRole recipientRole);
    
    /**
     * Find the default template by notification type and channel
     */
    Optional<NotificationTemplate> findByNotificationTypeAndChannelAndIsDefaultTrue(
        NotificationType notificationType, NotificationChannel channel);
    
    /**
     * Find template by notification type, channel and organization
     */
    Optional<NotificationTemplate> findByNotificationTypeAndChannelAndOrganizationIdAndIsActiveTrue(
        NotificationType notificationType, NotificationChannel channel, String organizationId);
    
    /**
     * Find all templates for a notification type
     */
    List<NotificationTemplate> findByNotificationType(NotificationType notificationType);
    
    /**
     * Find all active templates for a notification type
     */
    List<NotificationTemplate> findByNotificationTypeAndIsActiveTrue(NotificationType notificationType);
    
    /**
     * Find all templates by channel
     */
    List<NotificationTemplate> findByChannel(NotificationChannel channel);
    
    /**
     * Find all templates by organization
     */
    List<NotificationTemplate> findByOrganizationId(String organizationId);
    
    /**
     * Find all default templates
     */
    List<NotificationTemplate> findByIsDefaultTrue();
    
    /**
     * Find templates for priority notification types
     */
    @Query("{ 'notificationType': { $in: ?0 }, 'isActive': true }")
    List<NotificationTemplate> findByPriorityTypes(List<NotificationType> priorityTypes);
}
