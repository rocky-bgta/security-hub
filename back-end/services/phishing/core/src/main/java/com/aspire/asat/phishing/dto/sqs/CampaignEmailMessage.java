package com.aspire.asat.phishing.dto.sqs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQS message DTO carrying everything needed to send a single campaign email.
 * Fully self-contained so the consumer requires no additional database lookups.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignEmailMessage {

    private String campaignId;
    private String recipientId;
    private String clientId;
    private String trackingId;

    private String toEmail;
    private String recipientFirstName;
    private String recipientLastName;
    private String campaignName;

    private String fromAddress;
    private String displayName;

    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private boolean useTls;
    private boolean ignoreCertificateErrors;

    private String landingPageId;

    private String subject;
    private String htmlBody;
}
