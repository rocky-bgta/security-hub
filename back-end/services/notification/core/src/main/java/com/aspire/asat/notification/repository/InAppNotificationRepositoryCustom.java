package com.aspire.asat.notification.repository;

import com.aspire.asat.notification.model.InAppNotification;
import org.springframework.data.domain.Page;

/**
 * Custom repository interface for InAppNotification with advanced query methods
 */
public interface InAppNotificationRepositoryCustom {
    
    /**
     * Find all notifications for a user with optional search and pagination using offset
     */
    Page<InAppNotification> findByUserIdWithPagination(String userId, String search, int offset, int pageSize);
    
    /**
     * Find unread notifications for a user with optional search and pagination using offset
     */
    Page<InAppNotification> findUnreadByUserIdWithPagination(String userId, String search, int offset, int pageSize);

    /**
     * Find archived (read) notifications for a user with optional search and pagination using offset
     */
    Page<InAppNotification> findArchivedByUserIdWithPagination(String userId, String search, int offset, int pageSize);
}

