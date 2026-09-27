package com.aspire.asat.notification.repository;

import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.model.UserNotificationSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for individual user notification preferences.
 */
@Repository
public interface UserNotificationSettingsRepository extends MongoRepository<UserNotificationSettings, String> {

    Optional<UserNotificationSettings> findByUserIdAndNotificationType(String userId, NotificationType notificationType);

    boolean existsByUserIdAndNotificationType(String userId, NotificationType notificationType);

    List<UserNotificationSettings> findByUserId(String userId);

    void deleteByUserId(String userId);

    void deleteByUserIdAndNotificationType(String userId, NotificationType notificationType);
}
