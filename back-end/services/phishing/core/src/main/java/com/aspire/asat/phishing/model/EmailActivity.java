package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB entity for tracking email activities/events.
 * Records every interaction with phishing emails.
 */
@Document(collection = "email_activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "campaign_recipient_idx", def = "{'campaignId': 1, 'recipientId': 1}"),
    @CompoundIndex(name = "client_type_date_idx", def = "{'clientId': 1, 'activityType': 1, 'timestamp': -1}")
})
public class EmailActivity {

    @Id
    private String id;

    @Indexed
    private String clientId;

    @Indexed
    private String campaignId;

    private String recipientId;

    private String recipientName;

    private String recipientEmail;

    private String campaignName;

    @Indexed
    private String trackingId;

    private ActivityType activityType;

    /** Delivery channel of the parent campaign. Null on legacy rows. */
    private CampaignChannel channel;

    @Indexed
    private Instant timestamp;

    private String userAgent;

    private String ipAddress;

    private String geoLocation;

    private String deviceType;

    private String browser;

    private String operatingSystem;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
