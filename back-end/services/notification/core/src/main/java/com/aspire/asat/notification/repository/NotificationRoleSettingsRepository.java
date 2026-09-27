package com.aspire.asat.notification.repository;

import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.model.NotificationRoleSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for the role-based notification settings matrix.
 */
@Repository
public interface NotificationRoleSettingsRepository extends MongoRepository<NotificationRoleSettings, String> {

    Optional<NotificationRoleSettings> findByNotificationTypeAndRole(NotificationType notificationType, NotificationRecipientRole role);

    boolean existsByNotificationTypeAndRole(NotificationType notificationType, NotificationRecipientRole role);

    List<NotificationRoleSettings> findByNotificationType(NotificationType notificationType);

    List<NotificationRoleSettings> findByRole(NotificationRecipientRole role);

    List<NotificationRoleSettings> findAllByOrderByNotificationTypeAscRoleAsc();

    Page<NotificationRoleSettings> findAllByOrderByNotificationTypeAscRoleAsc(Pageable pageable);

    Page<NotificationRoleSettings> findByNotificationTypeOrderByRoleAsc(NotificationType notificationType, Pageable pageable);

    Page<NotificationRoleSettings> findByRoleOrderByNotificationTypeAsc(NotificationRecipientRole role, Pageable pageable);

    Page<NotificationRoleSettings> findByNotificationTypeAndRole(NotificationType notificationType,
                                                                  NotificationRecipientRole role,
                                                                  Pageable pageable);

    Page<NotificationRoleSettings> findByNotificationTypeInOrderByNotificationTypeAscRoleAsc(
            Collection<NotificationType> notificationTypes, Pageable pageable);

    Page<NotificationRoleSettings> findByNotificationTypeInAndRoleOrderByNotificationTypeAsc(
            Collection<NotificationType> notificationTypes, NotificationRecipientRole role, Pageable pageable);

    void deleteByNotificationTypeAndRole(NotificationType notificationType, NotificationRecipientRole role);
}
