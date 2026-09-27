package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.custom.CampaignRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for campaigns.
 */
@Repository
public interface CampaignRepository extends MongoRepository<Campaign, String>, CampaignRepositoryCustom {

    /**
     * Find all campaigns for a client with pagination
     */
    Page<Campaign> findByClientId(String clientId, Pageable pageable);

    /**
     * Find campaigns for a client filtered by campaign type
     */
    Page<Campaign> findByClientIdAndCampaignType(String clientId, CampaignType campaignType, Pageable pageable);

    /**
     * Find campaigns by client with search
     */
    @Query("{ 'clientId': ?0, 'campaignName': { $regex: ?1, $options: 'i' } }")
    Page<Campaign> searchByClientIdAndKeyword(String clientId, String keyword, Pageable pageable);

    /**
     * Find campaigns by client and status
     */
    Page<Campaign> findByClientIdAndStatus(String clientId, CampaignStatus status, Pageable pageable);

    /**
     * Find campaigns by client and multiple statuses
     */
    Page<Campaign> findByClientIdAndStatusIn(String clientId, List<CampaignStatus> statuses, Pageable pageable);

    /**
     * Find campaign by ID and client
     */
    Optional<Campaign> findByIdAndClientId(String id, String clientId);

    /**
     * Batch load campaigns for the client (e.g. report enrichment).
     */
    List<Campaign> findByIdInAndClientId(Collection<String> ids, String clientId);

    /**
     * Check if campaign name exists for client
     */
    boolean existsByClientIdAndCampaignName(String clientId, String campaignName);

    /**
     * Check if campaign name exists for client excluding specific campaign
     */
    boolean existsByClientIdAndCampaignNameAndIdNot(String clientId, String campaignName, String id);

    /**
     * Whether the client already has any campaign (any status) for the given productPackageId.
     */
    boolean existsByClientIdAndProductPackageId(String clientId, String productPackageId);

    /**
     * Same as {@link #existsByClientIdAndProductPackageId} but ignores the current campaign
     * (used after Step 1 create so the draft itself is not treated as a prior campaign).
     */
    boolean existsByClientIdAndProductPackageIdAndIdNot(
            String clientId, String productPackageId, String id);

    /**
     * Find scheduled campaigns ready to launch
     */
    @Query("{ 'status': 'SCHEDULED', 'schedule.startDateTime': { $lte: ?0 } }")
    List<Campaign> findScheduledCampaignsReadyToLaunch(Instant now);

    /**
     * Find campaigns by status and campaign type (e.g. RUNNING PHISHING_WITH_TRAINING campaigns
     * for the training-completion sweep).
     */
    List<Campaign> findByStatusAndCampaignType(CampaignStatus status, CampaignType campaignType);

    /**
     * Find campaigns by status and any of the given campaign types (e.g. RUNNING
     * PHISHING_WITH_TRAINING / SMISHING_WITH_TRAINING campaigns for the training-completion sweep).
     */
    List<Campaign> findByStatusAndCampaignTypeIn(CampaignStatus status, Collection<CampaignType> campaignTypes);

    /**
     * Find running or paused campaigns that are expired.
     */
    @Query("{ 'status': { $in: ['RUNNING', 'PAUSED'] }, 'expiresAt': { $lte: ?0 } }")
    List<Campaign> findRunningOrPausedCampaignsExpiredAtOrBefore(Instant now);

    /**
     * Count campaigns by client and status
     */
    long countByClientIdAndStatus(String clientId, CampaignStatus status);

    /**
     * Count campaigns by client with campaign-name search.
     */
    @Query(value = "{ 'clientId': ?0, 'campaignName': { $regex: ?1, $options: 'i' } }", count = true)
    long countByClientIdAndCampaignNameRegex(String clientId, String keyword);

    /**
     * Count all campaigns by client
     */
    long countByClientId(String clientId);

    /**
     * Count campaigns launched within {@code [start, end)}.
     */
    @Query(value = "{ 'clientId': ?0, 'launchedAt': { $gte: ?1, $lt: ?2 } }", count = true)
    long countByClientIdAndLaunchedAtBetween(String clientId, Instant start, Instant end);

    /**
     * Count campaigns of a given type launched within {@code [start, end)}.
     */
    @Query(value = "{ 'clientId': ?0, 'launchedAt': { $gte: ?1, $lt: ?2 }, 'campaignType': ?3 }", count = true)
    long countByClientIdAndLaunchedAtBetweenAndCampaignType(
            String clientId, Instant start, Instant end, CampaignType campaignType);

    /**
     * Find active campaigns (running or scheduled)
     */
    @Query("{ 'clientId': ?0, 'status': { $in: ['RUNNING', 'SCHEDULED'] } }")
    List<Campaign> findActiveCampaigns(String clientId);

    /**
     * Find campaigns using a specific email template
     */
    List<Campaign> findByClientIdAndEmailTemplateId(String clientId, String emailTemplateId);

    /**
     * Find campaigns using a specific landing page
     */
    List<Campaign> findByClientIdAndLandingPageId(String clientId, String landingPageId);

    /**
     * Find campaigns using a specific sender profile
     */
    List<Campaign> findByClientIdAndSenderProfileId(String clientId, String senderProfileId);

    /**
     * Find campaigns whose training module (CMS sub-package) ID is in the given set.
     * Used to resolve campaign names for phishing course enrollments.
     */
    @Query("{ 'clientId': ?0, 'trainingData.trainingModuleId': { $in: ?1 } }")
    List<Campaign> findByClientIdAndTrainingModuleIdIn(String clientId, Collection<String> trainingModuleIds);
}
