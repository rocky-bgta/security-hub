package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.RecipientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for campaign recipient data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRecipientDto {

    private String recipientId;
    private String campaignId;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String department;
    private String phoneNumber;
    private String countryName;
    private RecipientStatus status;

    // Timestamps
    private Instant emailSentAt;
    private Instant emailOpenedAt;
    private Instant linkClickedAt;
    private Instant dataSubmittedAt;
    private Instant reportedAt;

    // Interaction details
    private int openCount;
    private int clickCount;
    private boolean hasSubmittedData;
    private String activity;
}
