package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.phishing.controller.SenderProfileController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import com.aspire.asat.phishing.dto.request.SenderProfileRequest;
import com.aspire.asat.phishing.dto.response.SenderProfileImportResultDto;
import com.aspire.asat.phishing.dto.response.SenderProfileDto;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.SenderProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller implementation for sender profile management.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class SenderProfileControllerImpl implements SenderProfileController {

    private final SenderProfileService senderProfileService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<AllResponseDto<List<SenderProfileDto>>> getSenderProfiles(
            int offset, int pageSize, String searchParam, String clientId, ProfileType profileType,
            Boolean isVerified, String category, String targetIndustryId, String regionId, String language,
            String deceptionLevel, List<String> psychologicalTriggers, DomainType domainType,
            String personalizationLevel, ProviderType providerType, List<String> tags,
            String sortBy, String sortOrder) {
        
        try {
            List<SenderProfileDto> profiles = senderProfileService.getSenderProfiles(
                    offset, pageSize, searchParam, clientId, profileType, isVerified, category, targetIndustryId,
                    regionId, language, deceptionLevel, psychologicalTriggers, domainType, personalizationLevel,
                    providerType, tags, sortBy, sortOrder);
            long totalCount = senderProfileService.countSenderProfiles(searchParam, clientId, profileType, isVerified,
                    category, targetIndustryId, regionId, language, deceptionLevel, psychologicalTriggers,
                    domainType, personalizationLevel, providerType, tags);
            
            return ResponseEntity.ok(AllResponseDto.<List<SenderProfileDto>>builder()
                    .items(profiles)
                    .total(totalCount)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (Exception e) {
            log.error("Error getting sender profiles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AllResponseDto.<List<SenderProfileDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<SenderProfileDto>> getSenderProfileById(String profileId) {
        try {
            SenderProfileDto profile = senderProfileService.getSenderProfileById(profileId);
            return ResponseEntity.ok(ApiResponseDto.<SenderProfileDto>builder()
                    .data(profile)
                    .message("Sender profile retrieved successfully")
                    .build());
        } catch (RuntimeException e) {
            log.error("Error getting sender profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<SenderProfileDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<SenderProfileDto>> createSenderProfile(SenderProfileRequest request) {
        try {
            SenderProfileDto profile = senderProfileService.createSenderProfile(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.<SenderProfileDto>builder()
                            .data(profile)
                            .message(messageService.get(MessageKeys.SENDER_PROFILE_CREATED))
                            .build());
        } catch (RuntimeException e) {
            log.error("Error creating sender profile: {}", e.getMessage());
            HttpStatus status = e.getMessage().contains("already exists") 
                    ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(ApiResponseDto.<SenderProfileDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<SenderProfileDto>> updateSenderProfile(
            String profileId, SenderProfileRequest request) {
        try {
            SenderProfileDto profile = senderProfileService.updateSenderProfile(profileId, request);
            return ResponseEntity.ok(ApiResponseDto.<SenderProfileDto>builder()
                    .data(profile)
                    .message(messageService.get(MessageKeys.SENDER_PROFILE_UPDATED))
                    .build());
        } catch (ResourceNotFoundException e) {
            log.warn("Sender profile not found: {}", profileId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<SenderProfileDto>builder()
                            .message(e.getMessage())
                            .build());
        } catch (ServiceException e) {
            log.warn("Service error updating sender profile: {}", e.getMessage());
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponseDto.<SenderProfileDto>builder()
                                .message(e.getMessage())
                                .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.<SenderProfileDto>builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error updating sender profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<SenderProfileDto>builder()
                            .message("Failed to update sender profile")
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteSenderProfile(String profileId) {
        try {
            senderProfileService.deleteSenderProfile(profileId);
            return ResponseEntity.ok(ApiResponseDto.<String>builder()
                    .data(profileId)
                    .message(messageService.get(MessageKeys.SENDER_PROFILE_DELETED))
                    .build());
        } catch (ResourceNotFoundException e) {
            log.warn("Sender profile not found: {}", profileId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<String>builder()
                            .message(e.getMessage())
                            .build());
        } catch (ServiceException e) {
            log.warn("Service error deleting sender profile: {}", e.getMessage());
            if (e.getMessage().contains("active campaigns")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponseDto.<String>builder()
                                .message(e.getMessage())
                                .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.<String>builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error deleting sender profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<String>builder()
                            .message("Failed to delete sender profile")
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<SenderProfileDto>> duplicateSenderProfile(String profileId) {
        try {
            SenderProfileDto profile = senderProfileService.duplicateSenderProfile(profileId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.<SenderProfileDto>builder()
                            .data(profile)
                            .message(messageService.get(MessageKeys.SENDER_PROFILE_DUPLICATED))
                            .build());
        } catch (RuntimeException e) {
            log.error("Error duplicating sender profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<SenderProfileDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<TestResultDto>> testProfileConnection(String profileId) {
        try {
            TestResultDto result = senderProfileService.testProfileConnection(profileId);
            return ResponseEntity.ok(ApiResponseDto.<TestResultDto>builder()
                    .data(result)
                    .message(result.isSuccess() ? "Connection test successful" : "Connection test failed")
                    .build());
        } catch (RuntimeException e) {
            log.error("Error testing sender profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<TestResultDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<TestResultDto>> testNewConnection(SenderProfileRequest request) {
        try {
            TestResultDto result = senderProfileService.testNewConnection(request);
            return ResponseEntity.ok(ApiResponseDto.<TestResultDto>builder()
                    .data(result)
                    .message(result.isSuccess() ? "Connection test successful" : "Connection test failed")
                    .build());
        } catch (Exception e) {
            log.error("Error testing new connection: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDto.<TestResultDto>builder()
                    .data(TestResultDto.builder()
                            .success(false)
                            .message("Test failed: " + e.getMessage())
                            .build())
                    .message("Connection test failed")
                    .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<SenderProfileDto>>> getVerifiedProfiles() {
        try {
            List<SenderProfileDto> profiles = senderProfileService.getVerifiedProfiles();
            return ResponseEntity.ok(ApiResponseDto.<List<SenderProfileDto>>builder()
                    .data(profiles)
                    .message("Verified profiles retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting verified profiles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<List<SenderProfileDto>>builder()
                            .message("Failed to retrieve verified profiles")
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> checkDomainVerification(String email) {
        try {
            boolean isVerified = senderProfileService.isDomainVerified(email);
            return ResponseEntity.ok(ApiResponseDto.<Boolean>builder()
                    .data(isVerified)
                    .message(isVerified ? "Domain is verified" : "Domain is not verified")
                    .build());
        } catch (Exception e) {
            log.error("Error checking domain verification: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDto.<Boolean>builder()
                    .data(false)
                    .message("Failed to check domain verification")
                    .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<SenderProfileImportResultDto>> importSenderProfiles(MultipartFile file) {
        try {
            SenderProfileImportResultDto result = senderProfileService.importSenderProfiles(file);
            return ResponseEntity.ok(
                    new ApiResponseDto<>("Sender profiles imported successfully", 200, result));
        } catch (ServiceException e) {
            log.warn("Error importing sender profiles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error importing sender profiles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to import sender profiles", 500, null));
        }
    }
}
