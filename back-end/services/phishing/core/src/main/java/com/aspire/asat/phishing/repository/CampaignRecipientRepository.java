package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.repository.custom.CampaignRecipientRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for campaign recipients.
 */
@Repository
public interface CampaignRecipientRepository extends MongoRepository<CampaignRecipient, String>, CampaignRecipientRepositoryCustom {

    /**
     * Batch load recipients for the client (e.g. report enrichment).
     */
    List<CampaignRecipient> findByIdInAndClientId(Collection<String> ids, String clientId);

    /**
     * Find all recipients for a campaign with pagination
     */
    Page<CampaignRecipient> findByCampaignId(String campaignId, Pageable pageable);

    /**
     * Find all recipients for a campaign (no pagination)
     */
    List<CampaignRecipient> findByCampaignId(String campaignId);

    /**
     * Find recipients by campaign and status
     */
    List<CampaignRecipient> findByCampaignIdAndStatus(String campaignId, RecipientStatus status);

    /**
     * Find recipient by tracking ID
     */
    Optional<CampaignRecipient> findByTrackingId(String trackingId);

    /**
     * Find recipient by campaign and user
     */
    Optional<CampaignRecipient> findByCampaignIdAndUserId(String campaignId, String userId);

    /**
     * Find all recipients for a  user (across all campaigns).
     * Used to compute average phishing risk score per user.
     */
    List<CampaignRecipient> findByUserId(String userId);

    /**
     * Whether the user has been enrolled in any campaign for the given client.
     */
    boolean existsByClientIdAndUserId(String clientId, String userId);

    /**
     * Recipients for a client+user across all campaigns (end-user enrollment checks).
     */
    List<CampaignRecipient> findByClientIdAndUserId(String clientId, String userId);

    /**
     * Count recipient rows for a user among the given campaigns.
     */
    long countByClientIdAndUserIdAndCampaignIdIn(String clientId, String userId, Collection<String> campaignIds);

    /**
     * Count recipient rows for a user among the given campaigns with any of the given statuses.
     */
    long countByClientIdAndUserIdAndCampaignIdInAndStatusIn(
            String clientId, String userId, Collection<String> campaignIds, Collection<RecipientStatus> statuses);

    /**
     * Count recipient rows for a user among the given campaigns with an exact status.
     */
    long countByClientIdAndUserIdAndCampaignIdInAndStatus(
            String clientId, String userId, Collection<String> campaignIds, RecipientStatus status);

    /**
     * Find recipient by campaign and email
     */
    Optional<CampaignRecipient> findByCampaignIdAndEmail(String campaignId, String email);

    /**
     * Count recipients by campaign
     */
    long countByCampaignId(String campaignId);

    /**
     * Count recipients by campaign and status
     */
    long countByCampaignIdAndStatus(String campaignId, RecipientStatus status);

    /**
     * Count recipients by campaign and statuses
     */
    long countByCampaignIdAndStatusIn(String campaignId, List<RecipientStatus> statuses);

    /**
     * Recipients assigned training for a campaign (the denominator for training completion).
     */
    List<CampaignRecipient> findByCampaignIdAndTrainingAssignedTrue(String campaignId);

    /**
     * Count non-bounced recipients assigned training for a campaign (completion denominator).
     */
    @Query(value = "{ 'campaignId': ?0, 'trainingAssigned': true, 'status': { $ne: 'BOUNCED' } }", count = true)
    long countByCampaignIdAndTrainingAssignedTrue(String campaignId);

    /**
     * Count non-bounced assigned recipients whose training is completed (dashboard {@code complete}
     * or raw CMS {@code COMPLETED} / {@code PHISHING_TRAINING_COMPLETED}).
     */
    @Query(value = "{ 'campaignId': ?0, 'trainingAssigned': true, 'status': { $ne: 'BOUNCED' }, 'trainingStatus': { $in: ['complete', 'COMPLETED', 'PHISHING_TRAINING_COMPLETED'] } }", count = true)
    long countByCampaignIdAndTrainingAssignedTrueAndTrainingCompleted(String campaignId);

    /**
     * Delete all recipients for a campaign
     */
    void deleteByCampaignId(String campaignId);

    /**
     * Find pending recipients for sending
     */
    @Query("{ 'campaignId': ?0, 'status': 'PENDING' }")
    List<CampaignRecipient> findPendingRecipients(String campaignId, Pageable pageable);

    /**
     * Search recipients by email or name
     */
    @Query("{ 'campaignId': ?0, $or: [ " +
           "{ 'email': { $regex: ?1, $options: 'i' } }, " +
           "{ 'firstName': { $regex: ?1, $options: 'i' } }, " +
           "{ 'lastName': { $regex: ?1, $options: 'i' } } " +
           "] }")
    Page<CampaignRecipient> searchRecipients(String campaignId, String keyword, Pageable pageable);
}
