package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import com.aspire.asat.phishing.dto.enums.SendingPattern;
import com.aspire.asat.phishing.dto.sqs.CampaignEmailMessage;
import com.aspire.asat.phishing.model.*;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.service.CampaignEmailService;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignEmailProducer implements CampaignEmailService {

    private static final int MAX_SQS_DELAY_SECONDS = 900;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final CampaignRepository campaignRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final SenderProfileRepository senderProfileRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final LandingPageRepository landingPageRepository;
    private final TrackingBaseUrlResolver trackingBaseUrlResolver;
    private final CredentialEncryptionService credentialEncryptionService;

    @Value("${aws.sqs.campaign-email-queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.campaign-email-publish.max-attempts:2}")
    private int publishMaxAttempts;

    @Value("${aws.sqs.campaign-email-publish.initial-backoff-ms:250}")
    private long publishInitialBackoffMs;

    @Async
    @Override
    public void publishCampaignEmails(String campaignId) {
        log.info("Publishing campaign emails to SQS for campaign: {}", campaignId);

        try {
            Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new RuntimeException("Campaign not found: " + campaignId));

            EmailTemplate template = emailTemplateRepository.findById(campaign.getEmailTemplateId())
                    .orElseThrow(() -> new RuntimeException("Email template not found: " + campaign.getEmailTemplateId()));

            SenderProfile sender = senderProfileRepository.findById(campaign.getSenderProfileId())
                    .orElseThrow(() -> new RuntimeException("Sender profile not found: " + campaign.getSenderProfileId()));

            List<CampaignRecipient> pendingRecipients =
                    recipientRepository.findByCampaignIdAndStatus(campaignId, RecipientStatus.PENDING);

            if (pendingRecipients.isEmpty()) {
                log.warn("No pending recipients found for campaign: {}", campaignId);
                return;
            }

            LandingPage landingPage = null;
            if (campaign.getLandingPageId() != null) {
                landingPage = landingPageRepository.findById(campaign.getLandingPageId()).orElse(null);
            }
            String baseUrl = trackingBaseUrlResolver.resolve(
                    campaign.getClientId(),
                    campaign.getTrackingDomainId(),
                    landingPage != null ? landingPage.getTrackingDomainId() : null);
            log.info("Base url: {}", baseUrl);

            SendingConfig sendingConfig = campaign.getSchedule() != null
                    ? campaign.getSchedule().getSendingConfig()
                    : null;
            boolean staggered = sendingConfig != null
                    && sendingConfig.getPattern() == SendingPattern.STAGGERED
                    && sendingConfig.getBatchSize() > 0;

            int batchSize = pendingRecipients.size();
            int delayPerBatch = 0;
            if (staggered && sendingConfig != null) {
                batchSize = sendingConfig.getBatchSize();
                delayPerBatch = Math.min(sendingConfig.getBatchIntervalMinutes() * 60, MAX_SQS_DELAY_SECONDS);
            }

            // When STAGGERED: messages get SQS delaySeconds (batchIndex * delayPerBatch). Only the first
            // batch is visible immediately; later batches become visible after delay. The listener will
            // therefore receive messages in multiple polls (e.g. "2 received" then "2 received" later).
            if (staggered && delayPerBatch > 0) {
                log.info("Campaign {} uses STAGGERED sending: batchSize={}, delayPerBatch={}s. "
                        + "First {} messages visible now; rest visible after {}s per batch.",
                        campaignId, batchSize, delayPerBatch, Math.min(batchSize, pendingRecipients.size()), delayPerBatch);
            }

            int published = 0;
            List<String> failedRecipientIds = new ArrayList<>();
            for (int i = 0; i < pendingRecipients.size(); i++) {
                CampaignRecipient recipient = pendingRecipients.get(i);
                int batchIndex = i / batchSize;
                int delaySec = batchIndex * delayPerBatch;

                String personalizedSubject = personalize(template.getEmailSubject(), recipient);
                String personalizedBody = personalize(template.getEmailBody(), recipient);
                personalizedBody = instrumentHtml(personalizedBody, recipient.getTrackingId(), baseUrl);

                CampaignEmailMessage message = CampaignEmailMessage.builder()
                        .campaignId(campaignId)
                        .recipientId(recipient.getId())
                        .clientId(campaign.getClientId())
                        .trackingId(recipient.getTrackingId())
                        .toEmail(recipient.getEmail())
                        .recipientFirstName(recipient.getFirstName())
                        .recipientLastName(recipient.getLastName())
                        .campaignName(campaign.getCampaignName())
                        .fromAddress(sender.getFromAddress())
                        .displayName(sender.getDisplayName())
                        .smtpHost(sender.getHost())
                        .smtpPort(sender.getPort())
                        .smtpUsername(sender.getUsername())
                        .smtpPassword(credentialEncryptionService.decrypt(sender.getPassword()))
                        .useTls(sender.isUseTls())
                        .ignoreCertificateErrors(sender.isIgnoreCertificateErrors())
                        .landingPageId(campaign.getLandingPageId())
                        .subject(personalizedSubject)
                        .htmlBody(personalizedBody)
                        .build();

                boolean enqueueSuccess = publishToSqs(message, delaySec);
                if (enqueueSuccess) {
                    log.info("Pushed email to SQS with delaySec={} for recipient {} (batchIndex={})",
                            delaySec, recipient.getEmail(), batchIndex);
                    published++;
                } else {
                    failedRecipientIds.add(recipient.getId());
                    log.error("Failed to enqueue email to SQS for recipient {} (campaignId={}, batchIndex={})",
                            recipient.getId(), campaignId, batchIndex);
                }
            }

            log.info("Published {} email messages to SQS for campaign: {}", published, campaignId);
            if (!failedRecipientIds.isEmpty()) {
                throw new IllegalStateException("Failed to enqueue " + failedRecipientIds.size()
                        + " recipient message(s) for campaign " + campaignId
                        + ". Failed recipientIds=" + failedRecipientIds);
            }

        } catch (Exception e) {
            log.error("Failed to publish campaign emails for campaign: {}", campaignId, e);
        }
    }

    /**
     * Applies all tracking instrumentation to the email HTML body:
     * 1. Replace {{PHISHING_URL}} or {{PHISHING_LINK}} with the phish tracking URL
     * 2. Replace {{REPORT_URL}} with the report tracking URL
     * 3. Rewrite all remaining http(s) links to click-tracking URLs
     * 4. Inject an invisible 1x1 tracking pixel before &lt;/body&gt;
     */
    private String instrumentHtml(String html, String trackingId, String baseUrl) {
        if (html == null || html.isBlank()) {
            return html;
        }

        String phishUrl = baseUrl + "/t/phish/" + trackingId;
        String reportUrl = baseUrl + "/t/report/" + trackingId;

        html = html.replace("{{PHISHING_URL}}", phishUrl);
        html = html.replace("{{PHISHING_LINK}}", phishUrl);
        html = html.replace("{{REPORT_URL}}", reportUrl);
        html = ensureReportMarker(html, reportUrl);

        html = rewriteLinks(html, trackingId, baseUrl);
        html = injectTrackingPixel(html, trackingId, baseUrl);

        return html;
    }

    /**
     * Rewrites all {@code <a href>} attributes that point to external http(s) URLs
     * so they route through the click-tracking endpoint.
     * Links already pointing to the phish or report tracking endpoints are skipped.
     */
    private String rewriteLinks(String html, String trackingId, String baseUrl) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings().prettyPrint(false);

        String clickBase = baseUrl + "/t/click/" + trackingId + "?url=";
        String phishPrefix = baseUrl + "/t/phish/";
        String reportPrefix = baseUrl + "/t/report/";

        for (Element link : doc.select("a[href]")) {
            String href = link.attr("href");

            if (href.startsWith("mailto:") || href.startsWith("tel:") || href.startsWith("#")) {
                continue;
            }
            if (href.startsWith(phishPrefix) || href.startsWith(reportPrefix)) {
                continue;
            }
            if (!href.startsWith("http://") && !href.startsWith("https://")) {
                continue;
            }

            String encoded = URLEncoder.encode(href, StandardCharsets.UTF_8);
            link.attr("href", clickBase + encoded);
        }

        return doc.body().html();
    }

    /**
     * Appends an invisible 1x1 tracking pixel image tag before the closing
     * {@code </body>} tag. If no body tag exists, appends at the end.
     */
    private String injectTrackingPixel(String html, String trackingId, String baseUrl) {
        String pixelTag = "<img src=\"" + baseUrl + "/t/open/" + trackingId
                + "\" width=\"1\" height=\"1\" style=\"display:none\" alt=\"\">";

        if (html.contains("</body>")) {
            return html.replace("</body>", pixelTag + "</body>");
        }
        return html + pixelTag;
    }

    private boolean publishToSqs(CampaignEmailMessage message, int delaySeconds) {
        String messageBody;
        try {
            messageBody = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize SQS message for recipient: {}", message.getRecipientId(), e);
            return false;
        }

        for (int attempt = 1; attempt <= publishMaxAttempts; attempt++) {
            try {
                SendMessageRequest.Builder requestBuilder = SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody);

                if (delaySeconds > 0) {
                    requestBuilder.delaySeconds(delaySeconds);
                }

                sqsClient.sendMessage(requestBuilder.build());
                return true;
            } catch (Exception e) {
                boolean hasMoreAttempts = attempt < publishMaxAttempts;
                if (!hasMoreAttempts) {
                    log.error("Failed to publish SQS message for recipient {} after {} attempt(s)",
                            message.getRecipientId(), attempt, e);
                    return false;
                }

                long backoffMs = publishInitialBackoffMs * (1L << (attempt - 1));
                log.warn("SQS publish attempt {}/{} failed for recipient {}. Retrying in {} ms",
                        attempt, publishMaxAttempts, message.getRecipientId(), backoffMs, e);
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted during SQS publish retry backoff for recipient {}",
                            message.getRecipientId(), ie);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Ensures outbound phishing messages always contain an explicit report URL marker.
     * This helps Outlook add-ins discover the tracking ID even if templates do not include
     * a visible report button.
     */
    private String ensureReportMarker(String html, String reportUrl) {
        if (html == null || html.isBlank()) {
            return html;
        }
        if (html.contains("ASAT_REPORT_URL:") || html.contains("data-asat-report-url=\"true\"")) {
            return html;
        }

        // Keep both a comment marker and an invisible anchor fallback.
        // Some clients/remove transforms may strip comments from returned body HTML.
        String marker = "<!-- ASAT_REPORT_URL: " + reportUrl + " -->"
                + "<a href=\"" + reportUrl
                + "\" data-asat-report-url=\"true\" style=\"display:none !important;visibility:hidden;"
                + "mso-hide:all;font-size:0;line-height:0;max-height:0;max-width:0;overflow:hidden;\">"
                + "ASAT_REPORT_URL</a>";
        if (html.contains("</body>")) {
            return html.replace("</body>", marker + "</body>");
        }
        return html + marker;
    }

    private String personalize(String content, CampaignRecipient recipient) {
        if (content == null) return "";

        return content
                .replace("{{FIRST_NAME}}", nullSafe(recipient.getFirstName()))
                .replace("{{LAST_NAME}}", nullSafe(recipient.getLastName()))
                .replace("{{FULL_NAME}}", nullSafe(recipient.getFullName()))
                .replace("{{EMAIL_ADDRESS}}", nullSafe(recipient.getEmail()))
                .replace("{{DEPARTMENT}}", nullSafe(recipient.getDepartment()))
                .replace("{{organizationName}}", nullSafe(recipient.getOrganizationName()))
                .replace("{{organizationDomain}}", nullSafe(recipient.getOrganizationDomain()))
                .replace("{{domain}}", nullSafe(recipient.getOrganizationDomain()))
                .replace("{{PHONE_NUMBER}}", nullSafe(recipient.getPhoneNumber()))
                .replace("{{LOCATION}}", nullSafe(recipient.getCountryName()))
                .replace("{{TRACKING_ID}}", nullSafe(recipient.getTrackingId()));
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
