package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.CampaignSmsDelivery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampaignSmsDeliveryRepository extends MongoRepository<CampaignSmsDelivery, String> {

    Optional<CampaignSmsDelivery> findByCampaignIdAndRecipientId(String campaignId, String recipientId);
}
