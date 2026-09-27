package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.request.SenderProfileRequest;
import com.aspire.asat.phishing.dto.response.SenderProfileDto;
import com.aspire.asat.phishing.model.SenderProfile;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between SenderProfile entity and DTOs.
 */
@Component
public class SenderProfileMapper {

    /**
     * Convert entity to DTO
     */
    public SenderProfileDto toDto(SenderProfile entity, boolean canEdit, boolean canDelete) {
        if (entity == null) {
            return null;
        }

        return SenderProfileDto.builder()
                .profileId(entity.getId())
                .profileName(entity.getProfileName())
                .interfaceType(entity.getInterfaceType())
                .fromAddress(entity.getFromAddress())
                .displayName(entity.getDisplayName())
                .host(entity.getHost())
                .port(entity.getPort())
                .username(entity.getUsername())
                // password is NOT included for security
                .ignoreCertificateErrors(entity.isIgnoreCertificateErrors())
                .useTls(entity.isUseTls())
                .profileType(entity.getProfileType())
                .category(entity.getCategory())
                .targetIndustryId(entity.getTargetIndustryId())
                .regionId(entity.getRegionId())
                .language(entity.getLanguage())
                .deceptionLevel(entity.getDeceptionLevel())
                .psychologicalTriggers(entity.getPsychologicalTriggers())
                .domainType(entity.getDomainType())
                .domainName(entity.getDomainName())
                .personalizationLevel(entity.getPersonalizationLevel())
                .providerType(entity.getProviderType())
                .tags(entity.getTags())
                .replyToAddress(entity.getReplyToAddress())
                .isVerified(entity.isVerified())
                .lastTestedAt(entity.getLastTestedAt())
                .lastTestResult(entity.getLastTestResult())
                .canEdit(canEdit)
                .canDelete(canDelete)
                .isGlobal(entity.isGlobal())
                .createdByRole(entity.getCreatedByRole())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    /**
     * Convert request to entity (for creation)
     */
    public SenderProfile toEntity(SenderProfileRequest request, String clientId) {
        if (request == null) {
            return null;
        }

        return SenderProfile.builder()
                .clientId(clientId)
                .profileName(request.getProfileName())
                .interfaceType(request.getInterfaceType())
                .fromAddress(request.getFromAddress())
                .displayName(request.getDisplayName())
                .host(request.getHost())
                .port(request.getPort())
                .username(request.getUsername())
                .password(request.getPassword()) // Will be encrypted by service
                .ignoreCertificateErrors(request.isIgnoreCertificateErrors())
                .useTls(request.isUseTls())
                .category(request.getCategory())
                .targetIndustryId(request.getTargetIndustryId())
                .regionId(request.getRegionId())
                .language(request.getLanguage())
                .psychologicalTriggers(request.getPsychologicalTriggers())
                .domainType(request.getDomainType())
                .domainName(request.getDomainName())
                .providerType(request.getProviderType())
                .tags(request.getTags())
                .replyToAddress(request.getReplyToAddress())
                .profileType(ProfileType.CUSTOM)
                .isVerified(false)
                .build();
    }

    /**
     * Update entity from request
     */
    public void updateFromRequest(SenderProfile entity, SenderProfileRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setProfileName(request.getProfileName());
        entity.setInterfaceType(request.getInterfaceType());
        entity.setFromAddress(request.getFromAddress());
        entity.setDisplayName(request.getDisplayName());
        entity.setHost(request.getHost());
        entity.setPort(request.getPort());
        entity.setUsername(request.getUsername());
        
        // Only update password if provided (non-empty)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            entity.setPassword(request.getPassword()); // Will be encrypted by service
        }
        
        entity.setIgnoreCertificateErrors(request.isIgnoreCertificateErrors());
        entity.setUseTls(request.isUseTls());
        entity.setCategory(request.getCategory());
        entity.setTargetIndustryId(request.getTargetIndustryId());
        entity.setRegionId(request.getRegionId());
        entity.setLanguage(request.getLanguage());
        entity.setPsychologicalTriggers(request.getPsychologicalTriggers());
        entity.setDomainType(request.getDomainType());
        entity.setDomainName(request.getDomainName());
        entity.setProviderType(request.getProviderType());
        entity.setTags(request.getTags());
        entity.setReplyToAddress(request.getReplyToAddress());
        
        // Reset verification status when connection settings change
        entity.setVerified(false);
    }

    /**
     * Create a duplicate of an existing profile
     */
    public SenderProfile createDuplicate(SenderProfile original, String newName) {
        if (original == null) {
            return null;
        }

        return SenderProfile.builder()
                .clientId(original.getClientId())
                .profileName(newName)
                .interfaceType(original.getInterfaceType())
                .fromAddress(original.getFromAddress())
                .displayName(original.getDisplayName())
                .host(original.getHost())
                .port(original.getPort())
                .username(original.getUsername())
                .password(original.getPassword()) // Already encrypted
                .ignoreCertificateErrors(original.isIgnoreCertificateErrors())
                .useTls(original.isUseTls())
                .category(original.getCategory())
                .targetIndustryId(original.getTargetIndustryId())
                .regionId(original.getRegionId())
                .language(original.getLanguage())
                .deceptionLevel(original.getDeceptionLevel())
                .psychologicalTriggers(original.getPsychologicalTriggers())
                .domainType(original.getDomainType())
                .domainName(original.getDomainName())
                .personalizationLevel(original.getPersonalizationLevel())
                .providerType(original.getProviderType())
                .tags(original.getTags())
                .replyToAddress(original.getReplyToAddress())
                .profileType(ProfileType.CUSTOM) // Duplicates are always custom
                .isVerified(false) // Reset verification
                .build();
    }
}
