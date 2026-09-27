package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.SmsServerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * MongoDB entity for SMS server/provider configuration.
 */
@Document(collection = "sms_server_configurations")
@CompoundIndexes({
        @CompoundIndex(name = "client_default_idx", def = "{'clientId': 1, 'isDefault': 1}"),
        @CompoundIndex(name = "client_name_idx", def = "{'clientId': 1, 'name': 1}", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsServerConfiguration {

    @Id
    private String id;

    @NotBlank
    private String clientId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    private String provider;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String apiSecret;

    private String senderId;

    private String baseUrl;

    @Builder.Default
    private boolean isDefault = false;

    @Builder.Default
    private SmsServerStatus status = SmsServerStatus.ACTIVE;

    @Builder.Default
    private Map<String, String> providerMetadata = new HashMap<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}
