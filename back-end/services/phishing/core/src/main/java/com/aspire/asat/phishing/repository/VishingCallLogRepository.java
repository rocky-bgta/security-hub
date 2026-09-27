package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.VishingCallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VishingCallLogRepository extends MongoRepository<VishingCallLog, String> {

    Page<VishingCallLog> findByCampaignId(String campaignId, Pageable pageable);

    Optional<VishingCallLog> findByTrackingId(String trackingId);

    Optional<VishingCallLog> findByCampaignIdAndRecipientId(String campaignId, String recipientId);

    long countByCampaignId(String campaignId);

    long countByCampaignIdAndOutcome(String campaignId, com.aspire.asat.phishing.dto.enums.VishingCallOutcome outcome);
}
