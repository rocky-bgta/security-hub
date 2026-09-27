package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import com.aspire.asat.phishing.dto.request.SenderProfileRequest;
import com.aspire.asat.phishing.dto.response.SenderProfileImportResultDto;
import com.aspire.asat.phishing.dto.response.SenderProfileDto;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for sender profile management.
 */
public interface SenderProfileService {

    /**
     * Get all sender profiles for the current client with pagination
     */
    List<SenderProfileDto> getSenderProfiles(int offset, int pageSize, String searchParam, String clientId,
                                             ProfileType profileType, Boolean isVerified, String category,
                                             String targetIndustryId, String regionId, String language,
                                             String deceptionLevelId, List<String> psychologicalTriggers,
                                             DomainType domainType, String personalizationLevelId,
                                             ProviderType providerType, List<String> tags, String sortBy, String sortOrder);

    /**
     * Count total sender profiles matching criteria
     */
    long countSenderProfiles(String searchParam, String clientId, ProfileType profileType, Boolean isVerified,
                             String category, String targetIndustryId, String regionId, String language,
                             String deceptionLevelId, List<String> psychologicalTriggers, DomainType domainType,
                             String personalizationLevelId, ProviderType providerType, List<String> tags);

    /**
     * Get a sender profile by ID
     */
    SenderProfileDto getSenderProfileById(String profileId);

    /**
     * Create a new sender profile
     */
    SenderProfileDto createSenderProfile(SenderProfileRequest request);

    /**
     * Update an existing sender profile
     */
    SenderProfileDto updateSenderProfile(String profileId, SenderProfileRequest request);

    /**
     * Delete a sender profile
     */
    void deleteSenderProfile(String profileId);

    /**
     * Duplicate a sender profile
     */
    SenderProfileDto duplicateSenderProfile(String profileId);

    /**
     * Test SMTP connection for an existing profile
     */
    TestResultDto testProfileConnection(String profileId);

    /**
     * Test SMTP connection with new configuration (before saving)
     */
    TestResultDto testNewConnection(SenderProfileRequest request);

    /**
     * Check if profile name already exists
     */
    boolean isProfileNameExists(String profileName);

    /**
     * Get all verified profiles for the current client
     */
    List<SenderProfileDto> getVerifiedProfiles();

    /**
     * Check if from address domain is verified
     */
    boolean isDomainVerified(String fromAddress);

    /**
     * Bulk-import sender profiles from CSV.
     */
    SenderProfileImportResultDto importSenderProfiles(MultipartFile file);
}
