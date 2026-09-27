package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "microsoft_integrations")
public class MicrosoftIntegration {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clientAdminId;


    private String tenantId;
    private String tenantName;

    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiresAt;

    private boolean active;
    private Instant connectedAt;
    private Instant lastSyncAt;

    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;
}

