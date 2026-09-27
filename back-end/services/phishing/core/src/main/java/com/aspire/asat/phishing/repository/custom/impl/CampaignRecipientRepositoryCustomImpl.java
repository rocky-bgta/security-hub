package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.repository.custom.CampaignRecipientRepositoryCustom;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CampaignRecipientRepositoryCustomImpl implements CampaignRecipientRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean markSentByIdOrTrackingId(String recipientId, String trackingId, Instant emailSentAt) {
        Update update = new Update()
                .set("status", RecipientStatus.SENT)
                .set("emailSentAt", emailSentAt);

        // Idempotency guard: only transition from PENDING so we don't overwrite OPENED/CLICKED/etc.
        Criteria statusGuard = Criteria.where("status").is(RecipientStatus.PENDING);

        if (recipientId != null && !recipientId.isBlank()) {
            UpdateResult byId = mongoTemplate.updateFirst(
                    Query.query(new Criteria().andOperator(
                            Criteria.where("_id").is(recipientId),
                            statusGuard)),
                    update,
                    CampaignRecipient.class);
            if (byId.getModifiedCount() == 1L) {
                return true;
            }
        }

        if (trackingId != null && !trackingId.isBlank()) {
            UpdateResult byTracking = mongoTemplate.updateFirst(
                    Query.query(new Criteria().andOperator(
                            Criteria.where("trackingId").is(trackingId),
                            statusGuard)),
                    update,
                    CampaignRecipient.class);
            return byTracking.getModifiedCount() == 1L;
        }

        return false;
    }

    @Override
    public boolean markBouncedByIdOrTrackingId(String recipientId, String trackingId, Instant bouncedAt) {
        Update update = new Update()
                .set("status", RecipientStatus.BOUNCED)
                .set("bouncedAt", bouncedAt);

        // Allow downgrade only from PENDING or SENT to avoid clobbering more advanced statuses.
        Criteria statusGuard = Criteria.where("status").in(List.of(
                RecipientStatus.PENDING,
                RecipientStatus.SENT));

        if (recipientId != null && !recipientId.isBlank()) {
            UpdateResult byId = mongoTemplate.updateFirst(
                    Query.query(new Criteria().andOperator(
                            Criteria.where("_id").is(recipientId),
                            statusGuard)),
                    update,
                    CampaignRecipient.class);
            if (byId.getModifiedCount() == 1L) {
                return true;
            }
        }

        if (trackingId != null && !trackingId.isBlank()) {
            UpdateResult byTracking = mongoTemplate.updateFirst(
                    Query.query(new Criteria().andOperator(
                            Criteria.where("trackingId").is(trackingId),
                            statusGuard)),
                    update,
                    CampaignRecipient.class);
            return byTracking.getModifiedCount() == 1L;
        }

        return false;
    }

    @Override
    public long countByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return 0L;
        }
        return mongoTemplate.count(
                Query.query(Criteria.where("userId").is(userId.trim())),
                CampaignRecipient.class);
    }
}
