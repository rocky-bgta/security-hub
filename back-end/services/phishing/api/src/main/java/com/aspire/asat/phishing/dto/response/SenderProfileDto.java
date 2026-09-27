package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.InterfaceType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for sender profile data.
 * Password is not included in the response for security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderProfileDto {

    private String profileId;
    private String profileName;
    private InterfaceType interfaceType;
    private String fromAddress;
    private String displayName;
    private String host;
    private Integer port;
    private String username;
    // password is NOT returned for security
    private boolean ignoreCertificateErrors;
    private boolean useTls;
    private ProfileType profileType;
    private String category;
    private String targetIndustryId;
    private String regionId;
    private String language;
    private DeceptionLevelDto deceptionLevel;
    private List<String> psychologicalTriggers;
    private DomainType domainType;
    private String domainName;
    private PersonalizationLevelDto personalizationLevel;
    private ProviderType providerType;
    private List<String> tags;
    private String replyToAddress;
    private boolean isVerified;
    private Instant lastTestedAt;
    private String lastTestResult;
    private boolean canEdit;
    private boolean canDelete;
    private boolean isGlobal;
    private String createdByRole;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
