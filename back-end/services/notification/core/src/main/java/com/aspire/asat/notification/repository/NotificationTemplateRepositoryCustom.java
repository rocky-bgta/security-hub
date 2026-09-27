package com.aspire.asat.notification.repository;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.model.NotificationTemplate;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Custom repository interface for notification templates with dynamic, optional filters.
 *
 * <p>Derived query methods would need one variant per combination of the three optional
 * filters, so listing is expressed as a single criteria-based query instead.
 */
public interface NotificationTemplateRepositoryCustom {

    /**
     * Find templates matching the supplied filters. Any {@code null} filter is ignored.
     *
     * @param roleFilterPresent when true the {@code recipientRole} filter is applied even if it is
     *                          {@code null}, which selects the role-agnostic base templates
     */
    List<NotificationTemplate> search(NotificationType notificationType,
                                      NotificationChannel channel,
                                      NotificationRecipientRole recipientRole,
                                      boolean roleFilterPresent,
                                      Pageable pageable);

    /**
     * Count templates matching the same filters as {@link #search}.
     */
    long count(NotificationType notificationType,
               NotificationChannel channel,
               NotificationRecipientRole recipientRole,
               boolean roleFilterPresent);
}
