package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.PhishingRiskScore;
import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.TrackingService;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import com.aspire.asat.phishing.service.support.RecipientRiskScoringService;
import com.aspire.asat.phishing.service.support.RecipientTrainingAssignmentService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingServiceImpl implements TrackingService {

    private final CampaignRecipientRepository recipientRepository;
    private final CampaignRepository campaignRepository;
    private final EmailActivityRepository emailActivityRepository;
    private final LandingPageRepository landingPageRepository;
    private final TrackingBaseUrlResolver trackingBaseUrlResolver;
    private final CampaignCompletionEvaluator completionEvaluator;
    private final RecipientTrainingAssignmentService recipientTrainingAssignmentService;
    private final RecipientRiskScoringService recipientRiskScoringService;

    @Override
    public void recordOpen(String trackingId, String userAgent, String ipAddress) {
        recipientRepository.findByTrackingId(trackingId).ifPresent(recipient -> {
            if (isCampaignExpired(recipient.getCampaignId())) {
                log.debug("Skipping OPEN tracking for expired campaign, trackingId={}", trackingId);
                return;
            }
            
            if (shouldSkipRecipientUpdate(recipient, TrackingEvent.OPEN)) {
                log.debug("Skipping OPEN update for trackingId={} due to current status={}", trackingId, recipient.getStatus());
                return;
            }

            advanceStatus(recipient, RecipientStatus.OPENED);

            if (recipient.getEmailOpenedAt() == null) {
                recipient.setEmailOpenedAt(Instant.now());
            }
            recipient.setOpenCount(recipient.getOpenCount() + 1);
            recipient.setUserAgent(userAgent);
            recipient.setIpAddress(ipAddress);
            recipient.setRiskScore(PhishingRiskScore.OPENED.getPoints());
            recipientRepository.save(recipient);

            saveActivity(recipient, ActivityType.EMAIL_OPENED, userAgent, ipAddress, null,
                    recipient.getCampaignName());
            incrementCampaignStat(recipient.getCampaignId(), ActivityType.EMAIL_OPENED);
            recipientRiskScoringService.updateUserRiskProfilePhishingScore(recipient, ActivityType.EMAIL_OPENED);

            log.debug("Recorded EMAIL_OPENED for trackingId={}", trackingId);
        });
    }

    @Override
    public void recordClick(String trackingId, String userAgent, String ipAddress) {
        recipientRepository.findByTrackingId(trackingId).ifPresent(recipient -> {
            if (isCampaignExpired(recipient.getCampaignId())) {
                log.debug("Skipping CLICK tracking for expired campaign, trackingId={}", trackingId);
                return;
            }
            
            if (shouldSkipRecipientUpdate(recipient, TrackingEvent.CLICK)) {
                log.debug("Skipping CLICK update for trackingId={} due to current status={}", trackingId, recipient.getStatus());
                return;
            }


            advanceStatus(recipient, RecipientStatus.CLICKED);

            if (recipient.getLinkClickedAt() == null) {
                recipient.setLinkClickedAt(Instant.now());
            }
            recipient.setClickCount(recipient.getClickCount() + 1);
            recipient.setUserAgent(userAgent);
            recipient.setIpAddress(ipAddress);
            recipient.setRiskScore(PhishingRiskScore.CLICKED.getPoints());
            recipientRepository.save(recipient);

            saveActivity(recipient, ActivityType.LINK_CLICKED, userAgent, ipAddress, null,
                    recipient.getCampaignName());
            incrementCampaignStat(recipient.getCampaignId(), ActivityType.LINK_CLICKED);
            recipientRiskScoringService.updateUserRiskProfilePhishingScore(recipient, ActivityType.LINK_CLICKED);

            log.debug("Recorded LINK_CLICKED for trackingId={}", trackingId);
        });
    }

    @Override
    public String serveLandingPage(String trackingId, String userAgent, String ipAddress) {
        Optional<CampaignRecipient> recipientOpt = recipientRepository.findByTrackingId(trackingId);
        if (recipientOpt.isEmpty()) {
            log.warn("No recipient found for trackingId={}", trackingId);
            return null;
        }

        CampaignRecipient recipient = recipientOpt.get();

        if (isCampaignExpired(recipient.getCampaignId())) {
            log.debug("Skipping PHISH tracking update for expired campaign, trackingId={}", trackingId);
            return resolveLandingPageHtml(recipient.getCampaignId(), trackingId);
        }

        if (shouldSkipRecipientUpdate(recipient, TrackingEvent.PHISH)) {
            log.debug("Skipping PHISH update for trackingId={} due to current status={}", trackingId, recipient.getStatus());
            return resolveLandingPageHtml(recipient.getCampaignId(), trackingId);
        }

        advanceStatus(recipient, RecipientStatus.CLICKED);
        if (recipient.getLinkClickedAt() == null) {
            recipient.setLinkClickedAt(Instant.now());
        }
        recipient.setClickCount(recipient.getClickCount() + 1);
        recipient.setUserAgent(userAgent);
        recipient.setIpAddress(ipAddress);
        recipient.setRiskScore(PhishingRiskScore.CLICKED.getPoints());
        recipientRepository.save(recipient);


        log.debug("Recorded CLICK for trackingId={}, clickCount={}", trackingId, recipient.getClickCount());

        if (recipient.getClickCount() > 0) {
            log.debug("Click recorded for trackingId={}, assigning training if needed", trackingId);
            recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.CLICKED);
        }


        saveActivity(recipient, ActivityType.LINK_CLICKED, userAgent, ipAddress, null,
                recipient.getCampaignName());
        incrementCampaignStat(recipient.getCampaignId(), ActivityType.LINK_CLICKED);
        recipientRiskScoringService.updateUserRiskProfilePhishingScore(recipient, ActivityType.LINK_CLICKED);

        Optional<Campaign> campaignOpt = campaignRepository.findById(recipient.getCampaignId());
        if (campaignOpt.isEmpty() || campaignOpt.get().getLandingPageId() == null) {
            log.warn("No campaign or landing page for trackingId={}", trackingId);
            return null;
        }

        Campaign campaign = campaignOpt.get();
        Optional<LandingPage> pageOpt = landingPageRepository.findById(campaign.getLandingPageId());
        if (pageOpt.isEmpty()) {
            log.warn("Landing page {} not found for campaign {}", campaign.getLandingPageId(), campaign.getId());
            return null;
        }

        LandingPage landingPage = pageOpt.get();
        String baseUrl = resolveTrackingBaseUrl(campaign, landingPage);
        log.info("Base url resolved for campaign {}, landing page {}: {}", campaign.getId(), landingPage.getId(), baseUrl);
        return instrumentLandingPageHtml(landingPage.getHtmlContent(), trackingId, baseUrl);
    }

    @Override
    public String recordSubmission(String trackingId, Map<String, String> formData, String userAgent, String ipAddress) {
        Optional<CampaignRecipient> recipientOpt = recipientRepository.findByTrackingId(trackingId);

        if (recipientOpt.isEmpty()) {
            log.warn("No recipient found for trackingId={}", trackingId);
            return null;
        }

        CampaignRecipient recipient = recipientOpt.get();
        if (isCampaignExpired(recipient.getCampaignId())) {
            log.debug("Skipping SUBMIT tracking update for expired campaign, trackingId={}", trackingId);
            return resolveLandingPageRedirectUrl(recipient.getCampaignId());
        }
        
        if (shouldSkipRecipientUpdate(recipient, TrackingEvent.SUBMIT)) {
            log.debug("Skipping submit for trackingId={} due to current status={}", trackingId, recipient.getStatus());
            return resolveLandingPageRedirectUrl(recipient.getCampaignId());
        }

        advanceStatus(recipient, RecipientStatus.DATA_SUBMITTED);
        recipient.setDataSubmittedAt(Instant.now());
        recipient.setSubmittedData(new HashMap<>(formData));
        recipient.setUserAgent(userAgent);
        recipient.setIpAddress(ipAddress);
        recipient.setRiskScore(PhishingRiskScore.DATA_SUBMITTED.getPoints());
        recipientRepository.save(recipient);

        Map<String, Object> metadata = new HashMap<>(formData);
        saveActivity(recipient, ActivityType.DATA_SUBMITTED, userAgent, ipAddress, metadata,
                recipient.getCampaignName());
        incrementCampaignStat(recipient.getCampaignId(), ActivityType.DATA_SUBMITTED);
        recipientRiskScoringService.updateUserRiskProfilePhishingScore(recipientOpt.get(), ActivityType.DATA_SUBMITTED);
        log.info("Recorded DATA_SUBMITTED for trackingId={}, fields={}", trackingId, formData.keySet());

        recipientTrainingAssignmentService.assignTrainingSubPackageIfNeeded(recipient, RecipientStatus.DATA_SUBMITTED);

        return resolveLandingPageRedirectUrl(recipient.getCampaignId());
    }

    @Override
    public void recordReport(String trackingId, String userAgent, String ipAddress) {
        recordReport(trackingId, userAgent, ipAddress, null);
    }

    @Override
    public void recordReport(String trackingId, String userAgent, String ipAddress, Map<String, Object> metadata) {
        recipientRepository.findByTrackingId(trackingId).ifPresent(recipient -> {
            if (isCampaignExpired(recipient.getCampaignId())) {
                log.debug("Skipping REPORT tracking for expired campaign, trackingId={}", trackingId);
                return;
            }
            if (shouldSkipRecipientUpdate(recipient, TrackingEvent.REPORT)) {
                log.debug("Skipping REPORT update for trackingId={} due to current status={}", trackingId, recipient.getStatus());
                return;
            }

            advanceStatus(recipient, RecipientStatus.REPORTED);
            recipient.setReportedAt(Instant.now());
            recipient.setUserAgent(userAgent);
            recipient.setIpAddress(ipAddress);
            boolean hadOpened = recipient.getEmailOpenedAt() != null;
            recipient.setRiskScore(hadOpened
                    ? PhishingRiskScore.OPENED_BUT_REPORTED.getPoints()
                    : PhishingRiskScore.REPORTED_WITHOUT_OPEN.getPoints());
            recipientRepository.save(recipient);

            saveActivity(recipient, ActivityType.EMAIL_REPORTED, userAgent, ipAddress, metadata,
                    recipient.getCampaignName());
            incrementCampaignStat(recipient.getCampaignId(), ActivityType.EMAIL_REPORTED);
            recipientRiskScoringService.updateUserRiskProfilePhishingScore(recipient, ActivityType.EMAIL_REPORTED);

            log.info("Recorded EMAIL_REPORTED for trackingId={}", trackingId);
        });
    }

    // --- private helpers ---

    /**
     * Advances recipient status only if the new status is further along
     * the progression chain than the current status.
     * REPORTED is always allowed as a terminal override.
     */
    private void advanceStatus(CampaignRecipient recipient, RecipientStatus newStatus) {
        if (newStatus == RecipientStatus.REPORTED) {
            recipient.setStatus(RecipientStatus.REPORTED);
            return;
        }
        if (recipient.getStatus().ordinal() < newStatus.ordinal()) {
            recipient.setStatus(newStatus);
        }
    }

    private boolean shouldSkipRecipientUpdate(CampaignRecipient recipient, TrackingEvent event) {
        RecipientStatus currentStatus = recipient.getStatus();

        // Rule-1: Once compromised, do not mutate recipient via open/click/report/phish.
        if (currentStatus == RecipientStatus.DATA_SUBMITTED) {
            return event == TrackingEvent.OPEN
                    || event == TrackingEvent.CLICK
                    || event == TrackingEvent.REPORT
                    || event == TrackingEvent.PHISH
                    || event == TrackingEvent.SUBMIT;
        }

        // Rule-2: If already clicked, ignore open and report updates.
        if (currentStatus == RecipientStatus.CLICKED) {
            return event == TrackingEvent.OPEN || event == TrackingEvent.REPORT || event == TrackingEvent.PHISH;
        }

        // Rule-3: OPENED_BUT_REPORTED behavior maps to REPORTED with opened context.
        if (currentStatus == RecipientStatus.REPORTED) {
            return event == TrackingEvent.OPEN
                    || event == TrackingEvent.REPORT
                    || event == TrackingEvent.CLICK
                    || event == TrackingEvent.PHISH
                    || event == TrackingEvent.SUBMIT;
        }

        if (currentStatus == RecipientStatus.OPENED) {
            return event == TrackingEvent.OPEN;
        }

        return false;
    }

    private boolean isCampaignExpired(String campaignId) {
        if (campaignId == null || campaignId.isBlank()) {
            return false;
        }
        Optional<Campaign> campaignOpt = campaignRepository.findById(campaignId);
        if (campaignOpt.isEmpty()) {
            return false;
        }
        Campaign campaign = campaignOpt.get();
        if (!campaign.isExpired()) {
            return false;
        }
        if (campaign.getStatus() == CampaignStatus.RUNNING) {
            campaign.setStatus(CampaignStatus.EXPIRED);
            campaign.setCompletedAt(Instant.now());
            campaignRepository.save(campaign);
        }
        return true;
    }


    private void saveActivity(CampaignRecipient recipient, ActivityType type,
                              String userAgent, String ipAddress, Map<String, Object> metadata, String campaignName) {

        String recipientName = String.join(" ",
                recipient.getFirstName() == null ? "" : recipient.getFirstName().trim(),
                recipient.getLastName() == null ? "" : recipient.getLastName().trim()).trim();
        try {
            EmailActivity.EmailActivityBuilder builder = EmailActivity.builder()
                    .clientId(recipient.getClientId())
                    .campaignId(recipient.getCampaignId())
                    .recipientId(recipient.getId())
                    .recipientName(StringUtils.hasText(recipientName) ? recipientName : null)
                    .recipientEmail(recipient.getEmail())
                    .campaignName(campaignName)
                    .trackingId(recipient.getTrackingId())
                    .activityType(type)
                    .channel(resolveActivityChannel(recipient.getCampaignId()))
                    .timestamp(Instant.now())
                    .userAgent(userAgent)
                    .ipAddress(ipAddress);

            if (metadata != null) {
                builder.metadata(metadata);
            }

            emailActivityRepository.save(builder.build());
        } catch (Exception e) {
            log.error("Failed to save EmailActivity for recipient {}: {}", recipient.getId(), e.getMessage());
        }
    }

    private CampaignChannel resolveActivityChannel(String campaignId) {
        if (campaignId == null || campaignId.isBlank()) {
            return CampaignChannel.EMAIL;
        }
        return campaignRepository.findById(campaignId)
                .map(campaign -> DashboardChannelScope.effective(campaign.getChannel()))
                .orElse(CampaignChannel.EMAIL);
    }

    private void incrementCampaignStat(String campaignId, ActivityType type) {
        try {
            campaignRepository.findById(campaignId).ifPresent(campaign -> {
                CampaignStats stats = campaign.getStats();
                if (stats == null) {
                    stats = new CampaignStats();
                    campaign.setStats(stats);
                }

                switch (type) {
                    case EMAIL_OPENED -> stats.setEmailsOpened(stats.getEmailsOpened() + 1);
                    case LINK_CLICKED -> stats.setLinksClicked(stats.getLinksClicked() + 1);
                    case DATA_SUBMITTED -> stats.setDataSubmitted(stats.getDataSubmitted() + 1);
                    case EMAIL_REPORTED -> stats.setEmailsReported(stats.getEmailsReported() + 1);
                    default -> { }
                }

                stats.setLastUpdatedAt(Instant.now());
                campaign.setStats(stats);
                completionEvaluator.evaluateAndApply(campaign);
                campaignRepository.save(campaign);
                log.info("Incremented campaign stat for campaign {}, type={}, newStats={}", campaignId, type, stats);
            });
        } catch (Exception e) {
            log.error("Failed to update CampaignStats for campaign {}: {}", campaignId, e.getMessage());
        }
    }

    /**
     * Rewrites all {@code <form action>} attributes in the landing page HTML
     * to point to the tracking submission endpoint.
     */
    private String instrumentLandingPageHtml(String htmlContent, String trackingId, String baseUrl) {
        if (htmlContent == null || htmlContent.isBlank()) {
            return htmlContent;
        }

        String submitUrl = baseUrl + "/t/submit/" + trackingId;

        Document doc = Jsoup.parse(htmlContent);
        doc.outputSettings().prettyPrint(false);

        // Imported pages may include a meta CSP from the source website.
        // That CSP is applied by the browser and can block external CSS/JS
        // on our tracking endpoint even when gateway headers are relaxed.
        doc.select("meta[http-equiv]").removeIf(meta ->
                "content-security-policy".equalsIgnoreCase(meta.attr("http-equiv")));

        for (Element form : doc.select("form")) {
            form.attr("action", submitUrl);
            form.attr("method", "POST");
        }

        return doc.html();
    }

    private String resolveLandingPageRedirectUrl(String campaignId) {
        try {
            Optional<Campaign> campaignOpt = campaignRepository.findById(campaignId);
            if (campaignOpt.isEmpty() || campaignOpt.get().getLandingPageId() == null) {
                return null;
            }
            Optional<LandingPage> pageOpt = landingPageRepository.findById(campaignOpt.get().getLandingPageId());
            return pageOpt.map(LandingPage::getRedirectUrl).orElse(null);
        } catch (Exception e) {
            log.error("Failed to resolve redirect URL for campaign {}: {}", campaignId, e.getMessage());
            return null;
        }
    }

    private String resolveLandingPageHtml(String campaignId, String trackingId) {
        Optional<Campaign> campaignOpt = campaignRepository.findById(campaignId);
        if (campaignOpt.isEmpty() || campaignOpt.get().getLandingPageId() == null) {
            log.warn("No campaign or landing page for trackingId={}", trackingId);
            return null;
        }

        Campaign campaign = campaignOpt.get();
        Optional<LandingPage> pageOpt = landingPageRepository.findById(campaign.getLandingPageId());
        if (pageOpt.isEmpty()) {
            log.warn("Landing page {} not found for campaign {}", campaign.getLandingPageId(), campaign.getId());
            return null;
        }

        LandingPage landingPage = pageOpt.get();
        String baseUrl = resolveTrackingBaseUrl(campaign, landingPage);
        return instrumentLandingPageHtml(landingPage.getHtmlContent(), trackingId, baseUrl);
    }

    private String resolveTrackingBaseUrl(Campaign campaign, LandingPage landingPage) {
        return trackingBaseUrlResolver.resolve(
                campaign.getClientId(),
                campaign.getTrackingDomainId(),
                landingPage != null ? landingPage.getTrackingDomainId() : null);
    }

    /**
     * Delegates to {@link RecipientRiskScoringService} for backward compatibility with SMS consumer.
     */
    public void updateUserRiskProfilePhishingScore(CampaignRecipient recipient, ActivityType activityType) {
        recipientRiskScoringService.updateUserRiskProfilePhishingScore(recipient, activityType);
    }

    private enum TrackingEvent {
        OPEN,
        CLICK,
        REPORT,
        PHISH,
        SUBMIT
    }
}
