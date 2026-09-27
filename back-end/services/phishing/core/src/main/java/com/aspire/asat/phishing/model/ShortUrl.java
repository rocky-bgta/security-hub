package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Per-recipient shortened SMS link. {@code shortCode} maps to the existing
 * landing-page tracking URL so clicks/submissions stay on {@code /t/phish/{trackingId}}.
 */
@Document(collection = "short_urls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrl {

    @Id
    private String id;

    @Indexed(unique = true)
    private String shortCode;

    @Indexed(unique = true)
    private String trackingId;

    private String originalUrl;

    private String shortUrl;

    private String campaignId;

    private String recipientId;

    private String clientId;

    @CreatedDate
    private Instant createdAt;
}
