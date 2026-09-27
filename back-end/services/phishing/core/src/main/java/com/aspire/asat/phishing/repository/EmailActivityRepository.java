package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.repository.custom.EmailActivityRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB repository for email activities.
 */
@Repository
public interface EmailActivityRepository extends MongoRepository<EmailActivity, String>, EmailActivityRepositoryCustom {

    /**
     * Find activities by campaign
     */
    Page<EmailActivity> findByCampaignIdOrderByTimestampDesc(String campaignId, Pageable pageable);

    /**
     * Find activities by client with filters
     */
    Page<EmailActivity> findByClientIdAndTimestampBetweenOrderByTimestampDesc(
            String clientId, Instant startTime, Instant endTime, Pageable pageable);

    /**
     * Find activities by type
     */
    Page<EmailActivity> findByClientIdAndActivityTypeOrderByTimestampDesc(
            String clientId, ActivityType activityType, Pageable pageable);

    /**
     * Find activities by recipient
     */
    List<EmailActivity> findByRecipientIdOrderByTimestampDesc(String recipientId);

    /**
     * Find activities by tracking ID
     */
    List<EmailActivity> findByTrackingIdOrderByTimestampAsc(String trackingId);

    /**
     * Count activities by type in date range
     */
    long countByClientIdAndActivityTypeAndTimestampBetween(
            String clientId, ActivityType activityType, Instant startTime, Instant endTime);

    /**
     * Count by campaign and type
     */
    long countByCampaignIdAndActivityType(String campaignId, ActivityType activityType);

    /**
     * Find distinct campaign IDs where user clicked
     */
    @Query(value = "{ 'clientId': ?0, 'recipientId': ?1, 'activityType': 'LINK_CLICKED' }", 
           fields = "{ 'campaignId': 1 }")
    List<EmailActivity> findCampaignsWhereUserClicked(String clientId, String recipientId);

    /**
     * Count distinct users who clicked in a campaign
     */
    @Query(value = "{ 'campaignId': ?0, 'activityType': 'LINK_CLICKED' }", count = true)
    long countDistinctUsersWhoClicked(String campaignId);

    /**
     * Find recent activities
     */
    List<EmailActivity> findTop100ByClientIdOrderByTimestampDesc(String clientId);
}
