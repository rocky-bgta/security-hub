package com.aspire.asat.notification.repository;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.model.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoTemplate-based implementation of the dynamic notification template filters.
 */
@Repository
@RequiredArgsConstructor
public class NotificationTemplateRepositoryCustomImpl implements NotificationTemplateRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<NotificationTemplate> search(NotificationType notificationType,
                                             NotificationChannel channel,
                                             NotificationRecipientRole recipientRole,
                                             boolean roleFilterPresent,
                                             Pageable pageable) {
        Query query = buildQuery(notificationType, channel, recipientRole, roleFilterPresent);
        if (pageable != null) {
            query.with(pageable);
        }
        return mongoTemplate.find(query, NotificationTemplate.class);
    }

    @Override
    public long count(NotificationType notificationType,
                      NotificationChannel channel,
                      NotificationRecipientRole recipientRole,
                      boolean roleFilterPresent) {
        return mongoTemplate.count(
                buildQuery(notificationType, channel, recipientRole, roleFilterPresent),
                NotificationTemplate.class);
    }

    private Query buildQuery(NotificationType notificationType,
                             NotificationChannel channel,
                             NotificationRecipientRole recipientRole,
                             boolean roleFilterPresent) {
        Query query = new Query();

        if (notificationType != null) {
            query.addCriteria(Criteria.where("notificationType").is(notificationType));
        } else {
            // Hide unused-as-sender types from the admin template list.
            query.addCriteria(Criteria.where("notificationType").in(NotificationType.adminVisibleTypes()));
        }
        if (channel != null) {
            query.addCriteria(Criteria.where("channel").is(channel));
        }
        if (roleFilterPresent) {
            // A null role selects the base templates, which may store the field as null or omit it.
            query.addCriteria(Criteria.where("recipientRole").is(recipientRole));
        }

        return query;
    }
}
