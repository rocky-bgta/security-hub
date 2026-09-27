package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.SmsDeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Per-recipient SMS delivery record for provider tracking and audit.
 */
@Document(collection = "campaign_sms_deliveries")
@CompoundIndexes({
        @CompoundIndex(name = "campaign_recipient_idx", def = "{'campaignId': 1, 'recipientId': 1}", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSmsDelivery {

    @Id
    private String id;

    private String campaignId;

    private String recipientId;

    private String recipient;

    private String provider;

    private String messageId;

    @Builder.Default
    private SmsDeliveryStatus status = SmsDeliveryStatus.PENDING;

    private Instant sentAt;

    private Instant deliveredAt;

    private Instant clickedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
