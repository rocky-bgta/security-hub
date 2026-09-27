package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.dto.enums.VoiceServerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

@Document(collection = "voice_server_configurations")
@CompoundIndexes({
        @CompoundIndex(name = "client_default_idx", def = "{'clientId': 1, 'isDefault': 1}"),
        @CompoundIndex(name = "client_name_idx", def = "{'clientId': 1, 'name': 1}", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceServerConfiguration {

    @Id
    private String id;

    @NotBlank
    private String clientId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private VoiceProviderType provider;

    @NotBlank
    private String apiKey;

    @NotBlank
    private String apiSecret;

    private String callerId;

    private String baseUrl;

    private String region;

    private String countryCode;

    @Builder.Default
    private boolean isDefault = false;

    /**
     * Platform-admin created servers are visible to all clients (read-only for clients).
     * Tenant-created servers are scoped to {@link #clientId}.
     */
    @Builder.Default
    private boolean isGlobal = false;

    @Builder.Default
    private VoiceServerStatus status = VoiceServerStatus.ACTIVE;

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
