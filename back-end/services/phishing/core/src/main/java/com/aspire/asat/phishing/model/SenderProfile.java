package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.InterfaceType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import com.aspire.asat.phishing.dto.response.DeceptionLevelDto;
import com.aspire.asat.phishing.dto.response.PersonalizationLevelDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
import java.util.List;

/**
 * MongoDB entity for sender profiles.
 * Stores SMTP configuration for sending phishing campaign emails.
 */
@Document(collection = "sender_profiles")
@CompoundIndexes({
        @CompoundIndex(name = "deception_level_id_idx", def = "{'deceptionLevel.id': 1}"),
        @CompoundIndex(name = "personalization_level_id_idx", def = "{'personalizationLevel.id': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderProfile {

    @Id
    private String id;

    @NotBlank
    private String clientId;

    @NotBlank
    @Size(max = 100)
    private String profileName;

    @NotNull
    private InterfaceType interfaceType;

    @NotBlank
    @Email
    private String fromAddress;

    private String displayName;

    @NotBlank
    private String host;

    @NotNull
    private Integer port;

    @NotBlank
    private String username;

    @NotBlank
    private String password;  // Encrypted at rest

    @Builder.Default
    private boolean ignoreCertificateErrors = false;

    @Builder.Default
    private boolean useTls = true;

    @Builder.Default
    private ProfileType profileType = ProfileType.CUSTOM;

    private String category;

    private String targetIndustryId;

    private String regionId;

    private String language;

    /**
     * Embedded snapshot from {@code deception_levels} catalog (optional).
     */
    private DeceptionLevelDto deceptionLevel;

    private List<String> psychologicalTriggers;

    private DomainType domainType;

    private String domainName;

    /**
     * Embedded snapshot from {@code personalization_levels} catalog (optional).
     */
    private PersonalizationLevelDto personalizationLevel;

    private ProviderType providerType;

    private List<String> tags;

    @Email
    private String replyToAddress;

    @Builder.Default
    private boolean isGlobal = false;

    @Builder.Default
    private boolean isVerified = false;

    private Instant lastTestedAt;

    private String lastTestResult;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

//    @CreatedBy
    private String createdBy;

    private String createdByRole;

//    @LastModifiedBy
    private String lastModifiedBy;
}
