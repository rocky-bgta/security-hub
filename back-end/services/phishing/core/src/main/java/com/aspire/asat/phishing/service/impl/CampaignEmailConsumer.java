package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.sqs.CampaignEmailMessage;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Properties;

/**
 * Consumes campaign email messages from SQS, sends each email via
 * the campaign's SMTP sender profile, and updates recipient/campaign state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignEmailConsumer {

    private final CampaignRecipientRepository recipientRepository;
    private final CampaignRepository campaignRepository;
    private final EmailActivityRepository emailActivityRepository;
    private final TrackingServiceImpl trackingService;
    private final CampaignCompletionEvaluator completionEvaluator;

    public void processEmailMessage(CampaignEmailMessage message) {
        log.info("Sending campaign email to {} for campaign {}", message.getToEmail(), message.getCampaignId());
        log.debug("CampaignEmailMessage details: campaignId={}, recipientId={}, trackingId={}, smtpHost={}, smtpPort={}, useTls={}",
                message.getCampaignId(), message.getRecipientId(), message.getTrackingId(),
                message.getSmtpHost(), message.getSmtpPort(), message.isUseTls());

        // Do not send (nor record activity) for campaigns that are no longer active (expired/cancelled/completed).
        if (isCampaignInactiveForSending(message.getCampaignId())) {
            log.info("Skipping send for inactive/expired campaign {} (recipientId={}, email={})",
                    message.getCampaignId(), message.getRecipientId(), message.getToEmail());
            return;
        }

        boolean smtpSucceeded;
        String smtpError = null;
        try {
            JavaMailSenderImpl mailSender = createMailSender(message);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(new InternetAddress(message.getFromAddress(), message.getDisplayName()));
            helper.setTo(message.getToEmail());
            helper.setSubject(message.getSubject());
            helper.setText(message.getHtmlBody(), true);

            mailSender.send(mimeMessage);
            smtpSucceeded = true;
        } catch (Exception e) {
            if (isTransientSmtpFailure(e)) {
                log.warn("Transient SMTP failure for {} (campaignId={}, recipientId={}, trackingId={}). "
                                + "Message will be retried by SQS visibility timeout. Error={}",
                        message.getToEmail(), message.getCampaignId(), message.getRecipientId(),
                        message.getTrackingId(), e.getMessage(), e);
                throw new TransientSmtpFailureException("Transient SMTP failure", e);
            }
            smtpSucceeded = false;
            smtpError = e.getMessage();
            log.error("SMTP send failed for {} (campaignId={}, recipientId={}, trackingId={}): {}",
                    message.getToEmail(), message.getCampaignId(), message.getRecipientId(),
                    message.getTrackingId(), smtpError, e);
        }

        if (smtpSucceeded) {
            onSendSuccess(message);
        } else {
            onSendFailure(message, smtpError);
        }
    }

    private boolean isTransientSmtpFailure(Exception exception) {
        String combined = flattenExceptionMessages(exception).toLowerCase();
        return combined.contains("timeout")
                || combined.contains("timed out")
                || combined.contains("connection reset")
                || combined.contains("connection refused")
                || combined.contains("could not connect")
                || combined.contains("service unavailable")
                || combined.contains("throttl")
                || combined.contains("rate exceeded")
                || combined.contains("temporary")
                || combined.contains("try again later")
                || combined.matches(".*\\b4\\d\\d\\b.*");
    }

    private String flattenExceptionMessages(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null) {
                builder.append(current.getMessage()).append(' ');
            }
            current = current.getCause();
        }
        return builder.toString();
    }

    private JavaMailSenderImpl createMailSender(CampaignEmailMessage message) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(message.getSmtpHost());
        mailSender.setPort(message.getSmtpPort());
        mailSender.setUsername(message.getSmtpUsername());
        mailSender.setPassword(message.getSmtpPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        if (message.isUseTls()) {
            if (message.getSmtpPort() == 465) {
                props.put("mail.smtp.ssl.enable", "true");
            } else {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
            }
        }

        if (message.isIgnoreCertificateErrors()) {
            props.put("mail.smtp.ssl.trust", "*");
        }

        return mailSender;
    }

    private void onSendSuccess(CampaignEmailMessage message) {
        Instant now = Instant.now();

        try {
            boolean updated = recipientRepository.markSentByIdOrTrackingId(
                    message.getRecipientId(), message.getTrackingId(), now);
            if (!updated) {
                log.error("SENT update did not match any recipient (already advanced or missing). "
                        + "campaignId={}, recipientId={}, trackingId={}, email={}",
                        message.getCampaignId(), message.getRecipientId(),
                        message.getTrackingId(), message.getToEmail());
            }
        } catch (Exception e) {
            log.error("Atomic SENT update failed for campaignId={}, recipientId={}, trackingId={}: {}",
                    message.getCampaignId(), message.getRecipientId(),
                    message.getTrackingId(), e.getMessage(), e);
        }

        saveActivity(message, ActivityType.EMAIL_SENT);
        incrementStat(message.getCampaignId(), true);

        recipientRepository.findById(message.getRecipientId())
                .ifPresent(recipient ->
                        trackingService.updateUserRiskProfilePhishingScore(recipient, ActivityType.EMAIL_SENT));

        log.info("Email sent successfully to {} for campaign {}", message.getToEmail(), message.getCampaignId());
    }

    private void onSendFailure(CampaignEmailMessage message, String errorMessage) {
        Instant now = Instant.now();

        try {
            boolean updated = recipientRepository.markBouncedByIdOrTrackingId(
                    message.getRecipientId(), message.getTrackingId(), now);
            if (!updated) {
                log.error("BOUNCED update did not match any recipient (already advanced or missing). "
                        + "campaignId={}, recipientId={}, trackingId={}, email={}, smtpError={}",
                        message.getCampaignId(), message.getRecipientId(),
                        message.getTrackingId(), message.getToEmail(), errorMessage);
            }
        } catch (Exception e) {
            log.error("Atomic BOUNCED update failed for campaignId={}, recipientId={}, trackingId={}: {}",
                    message.getCampaignId(), message.getRecipientId(),
                    message.getTrackingId(), e.getMessage(), e);
        }

        saveActivity(message, ActivityType.EMAIL_BOUNCED);
        incrementStat(message.getCampaignId(), false);
    }

    private void saveActivity(CampaignEmailMessage message, ActivityType activityType) {
        try {
            String recipientName = String.join(" ",
                    message.getRecipientFirstName() == null ? "" : message.getRecipientFirstName().trim(),
                    message.getRecipientLastName() == null ? "" : message.getRecipientLastName().trim()).trim();
            EmailActivity activity = EmailActivity.builder()
                    .clientId(message.getClientId())
                    .campaignId(message.getCampaignId())
                    .recipientId(message.getRecipientId())
                    .recipientName(StringUtils.hasText(recipientName) ? recipientName : null)
                    .recipientEmail(message.getToEmail())
                    .campaignName(message.getCampaignName())
                    .trackingId(message.getTrackingId())
                    .activityType(activityType)
                    .channel(CampaignChannel.EMAIL)
                    .timestamp(Instant.now())
                    .build();
            emailActivityRepository.save(activity);
        } catch (Exception e) {
            log.error("Failed to save email activity for recipient {}: {}",
                    message.getRecipientId(), e.getMessage());
        }
    }

    private void incrementStat(String campaignId, boolean success) {
        try {
            campaignRepository.findById(campaignId).ifPresent(campaign -> {
                if (success) {
                    campaign.getStats().setEmailsSent(campaign.getStats().getEmailsSent() + 1);
                } else {
                    campaign.getStats().setEmailsBounced(campaign.getStats().getEmailsBounced() + 1);
                }
                campaign.getStats().setLastUpdatedAt(Instant.now());

                completionEvaluator.evaluateAndApply(campaign);
                campaignRepository.save(campaign);
            });
        } catch (Exception e) {
            log.error("Failed to update campaign stats for {}: {}", campaignId, e.getMessage());
        }
    }

    /**
     * Returns true when the campaign no longer permits sending: expired, cancelled, or completed.
     * Expired RUNNING campaigns are not flipped here (handled by the expiry scheduler / tracking checks);
     * this is a best-effort drain guard so in-flight SQS messages are not delivered after expiry.
     */
    private boolean isCampaignInactiveForSending(String campaignId) {
        if (campaignId == null || campaignId.isBlank()) {
            return false;
        }
        return campaignRepository.findById(campaignId)
                .map(campaign -> campaign.isExpired()
                        || campaign.getStatus() == CampaignStatus.EXPIRED
                        || campaign.getStatus() == CampaignStatus.CANCELLED
                        || campaign.getStatus() == CampaignStatus.COMPLETED)
                .orElse(false);
    }

    private static class TransientSmtpFailureException extends RuntimeException {
        private TransientSmtpFailureException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
