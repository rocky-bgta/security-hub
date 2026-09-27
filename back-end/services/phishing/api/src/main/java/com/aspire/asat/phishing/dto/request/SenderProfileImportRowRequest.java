package com.aspire.asat.phishing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Raw row DTO parsed from sender profile CSV import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderProfileImportRowRequest {
    private String profileName;
    private String interfaceType;
    private String fromAddress;
    private String displayName;
    private String host;
    private String port;
    private String username;
    private String password;
    private String useTls;
    private String ignoreCertificateErrors;
    private String category;
    private String targetIndustryId;
    private String regionId;
    private String language;
    private String deceptionLevel;
    private String psychologicalTriggers;
    private String domainType;
    private String domainName;
    private String personalizationLevel;
    private String providerType;
    private String tags;
    private String replyToAddress;
}
