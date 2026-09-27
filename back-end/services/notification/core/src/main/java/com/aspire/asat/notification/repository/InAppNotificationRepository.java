package com.aspire.asat.notification.repository;

import com.aspire.asat.notification.model.InAppNotification;
import com.aspire.asat.common.enums.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for InAppNotification document
 */
@Repository
public interface InAppNotificationRepository extends MongoRepository<InAppNotification, String>, InAppNotificationRepositoryCustom {
    
    /**
     * Find all notifications for a user
     */
    List<InAppNotification> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find all notifications for a user with pagination
     */
    Page<InAppNotification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * Find all notifications for a user with search and pagination
     */
    @Query(value = "{ 'user_id': ?0, '$or': [ { 'title': { $regex: ?1, $options: 'i' } }, { 'message': { $regex: ?1, $options: 'i' } } ] }", sort = "{ 'created_at': -1 }")
    Page<InAppNotification> findByUserIdAndSearchOrderByCreatedAtDesc(String userId, String search, Pageable pageable);
    
    /**
     * Find unread notifications for a user
     */
    List<InAppNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);
    
    /**
     * Find unread notifications for a user with pagination
     */
    Page<InAppNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * Find unread notifications for a user with search and pagination
     */
    @Query(value = "{ 'user_id': ?0, 'is_read': false, '$or': [ { 'title': { $regex: ?1, $options: 'i' } }, { 'message': { $regex: ?1, $options: 'i' } } ] }", sort = "{ 'created_at': -1 }")
    Page<InAppNotification> findByUserIdAndIsReadFalseAndSearchOrderByCreatedAtDesc(String userId, String search, Pageable pageable);
    
    /**
     * Find read notifications for a user
     */
    List<InAppNotification> findByUserIdAndIsReadTrueOrderByCreatedAtDesc(String userId);
    
    /**
     * Find notifications by user and notification type
     */
    List<InAppNotification> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(
        String userId, NotificationType notificationType);
    
    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndIsReadFalse(String userId);
    
    /**
     * Find notifications created after a specific date
     */
    List<InAppNotification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
        String userId, LocalDateTime createdAt);
    
    /**
     * Find notifications by notification type
     */
    List<InAppNotification> findByNotificationTypeOrderByCreatedAtDesc(NotificationType notificationType);
    
    /**
     * Delete old notifications (for cleanup)
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
    
    /**
     * Mark all notifications as read for a user
     */
    @Query("{ 'userId': ?0, 'isRead': false }")
    @Update("{ '$set': { 'isRead': true, 'readAt': ?1 } }")
    void markAllAsReadForUser(String userId, LocalDateTime readAt);
}
