package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.client.CmsPhishingCourseClient;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCourseEnrollmentDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCoursePageDto;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.service.impl.CampaignCompletionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Periodically reconciles training completion for RUNNING {@code PHISHING_WITH_TRAINING} and
 * {@code SMISHING_WITH_TRAINING} campaigns by pulling per-user enrollment status from CMS, and
 * transitions a campaign to COMPLETED once every recipient that was assigned training has finished it.
 *
 * <p>Expiry always takes precedence: expired campaigns are left for {@code CampaignExpiryScheduler}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignTrainingCompletionScheduler {

    /** Page size for CMS phishing-course detail lookups. */
    private static final int CMS_PAGE_SIZE = 500;

    /** Max userIds sent per CMS request to bound URL length. */
    private static final int USER_ID_BATCH_SIZE = 100;

    /** Campaign types whose completion is gated on CMS training completion. */
    private static final List<CampaignType> TRAINING_CAMPAIGN_TYPES =
            List.of(CampaignType.PHISHING_WITH_TRAINING, CampaignType.SMISHING_WITH_TRAINING);

    private final CampaignRepository campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final CmsPhishingCourseClient cmsPhishingCourseClient;
    private final CampaignCompletionEvaluator completionEvaluator;

    @Scheduled(fixedDelayString = "${campaign.training-completion.check-ms:300000}")
    public void sweepTrainingCampaigns() {
        List<Campaign> campaigns = campaignRepository.findByStatusAndCampaignTypeIn(
                CampaignStatus.RUNNING, TRAINING_CAMPAIGN_TYPES);
        if (campaigns.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        for (Campaign campaign : campaigns) {
            try {
                reconcileCampaign(campaign, now);
            } catch (Exception ex) {
                log.error("Training-completion sweep failed for campaign {}", campaign.getId(), ex);
            }
        }
    }

    private void reconcileCampaign(Campaign campaign, Instant now) {
        if (campaign.isExpiredAt(now)) {
            // Expiry wins; CampaignExpiryScheduler will mark it EXPIRED.
            return;
        }

        List<CampaignRecipient> assigned =
                recipientRepository.findByCampaignIdAndTrainingAssignedTrue(campaign.getId());

        boolean statsRefreshed = false;
        if (!assigned.isEmpty()) {
            String moduleId = campaign.getTrainingData() != null
                    ? campaign.getTrainingData().getTrainingModuleId() : null;
            if (moduleId == null || moduleId.isBlank()) {
                log.warn("Campaign {} has {} training-assigned recipients but no trainingModuleId; skipping",
                        campaign.getId(), assigned.size());
                return;
            }
            statsRefreshed = refreshTrainingStatuses(campaign, assigned, moduleId);
        }

        if (completionEvaluator.evaluateAndApply(campaign)) {
            campaignRepository.save(campaign);
            log.info("Campaign {} transitioned to COMPLETED after training-completion sweep", campaign.getId());
        } else if (statsRefreshed) {
            // Persist refreshed training counts even when not yet complete.
            campaignRepository.save(campaign);
        }
    }

    private boolean refreshTrainingStatuses(Campaign campaign, List<CampaignRecipient> assigned, String moduleId) {
        Set<String> userIds = assigned.stream()
                .map(CampaignRecipient::getUserId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userIds.isEmpty()) {
            return false;
        }

        Map<String, String> statusByUserId = fetchTrainingStatuses(campaign.getClientId(), moduleId, userIds);
        if (statusByUserId.isEmpty()) {
            log.debug("No CMS training statuses resolved for campaign {} (module {})", campaign.getId(), moduleId);
            return false;
        }

        Instant now = Instant.now();
        int completedCount = 0;
        List<CampaignRecipient> toSave = new ArrayList<>();
        for (CampaignRecipient recipient : assigned) {
            String latestStatus = statusByUserId.get(recipient.getUserId());
            boolean changed = false;
            if (latestStatus != null && !latestStatus.equalsIgnoreCase(recipient.getTrainingStatus())) {
                recipient.setTrainingStatus(latestStatus);
                changed = true;
            }
            if (recipient.isTrainingCompleted()) {
                completedCount++;
                if (recipient.getTrainingCompletedAt() == null) {
                    recipient.setTrainingCompletedAt(now);
                    changed = true;
                }
            }
            if (changed) {
                toSave.add(recipient);
            }
        }
        if (!toSave.isEmpty()) {
            recipientRepository.saveAll(toSave);
        }

        if (campaign.getStats() != null) {
            campaign.getStats().setTrainingAssignedCount(assigned.size());
            campaign.getStats().setTrainingCompletedCount(completedCount);
            campaign.getStats().setLastUpdatedAt(now);
        }
        return true;
    }

    /**
     * Fetches the latest CMS enrollment status for the given users, keyed by userId, restricted to the
     * campaign's training sub-package. UserIds are batched to keep request URLs bounded.
     */
    private Map<String, String> fetchTrainingStatuses(String clientId, String moduleId, Set<String> userIds) {
        Map<String, String> statusByUserId = new HashMap<>();
        List<String> ids = new ArrayList<>(userIds);
        for (int start = 0; start < ids.size(); start += USER_ID_BATCH_SIZE) {
            List<String> batch = ids.subList(start, Math.min(start + USER_ID_BATCH_SIZE, ids.size()));
            collectBatchStatuses(clientId, moduleId, batch, statusByUserId);
        }
        return statusByUserId;
    }

    private void collectBatchStatuses(String clientId, String moduleId, List<String> batch,
                                      Map<String, String> statusByUserId) {
        int page = 0;
        while (true) {
            CmsPhishingCoursePageDto response =
                    cmsPhishingCourseClient.getDetails(clientId, page, CMS_PAGE_SIZE, batch);
            List<CmsPhishingCourseEnrollmentDto> items = response.getItems() == null
                    ? Collections.emptyList() : response.getItems();

            for (CmsPhishingCourseEnrollmentDto enrollment : items) {
                if (moduleId.equals(enrollment.getSubPackageId()) && enrollment.getUserId() != null) {
                    statusByUserId.put(enrollment.getUserId(), enrollment.getStatus());
                }
            }

            long total = response.getTotal() != null ? response.getTotal() : items.size();
            page++;
            if (items.isEmpty() || (long) page * CMS_PAGE_SIZE >= total) {
                break;
            }
        }
    }
}
