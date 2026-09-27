package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.enums.DomainType;
import com.aspire.asat.phishing.dto.enums.InterfaceType;
import com.aspire.asat.phishing.dto.enums.ProviderType;
import com.aspire.asat.phishing.dto.response.DeceptionLevelDto;
import com.aspire.asat.phishing.dto.response.PersonalizationLevelDto;
import com.aspire.asat.phishing.dto.request.SenderProfileImportRowRequest;
import com.aspire.asat.phishing.dto.request.SenderProfileRequest;
import com.aspire.asat.phishing.dto.response.SenderProfileImportErrorDto;
import com.aspire.asat.phishing.dto.response.SenderProfileImportResultDto;
import com.aspire.asat.phishing.dto.response.SenderProfileDto;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.SenderProfileMapper;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.DomainRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.service.SenderProfileService;
import com.aspire.asat.phishing.service.SmtpTestService;
import com.aspire.asat.phishing.service.support.CatalogReferenceResolver;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for sender profile management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SenderProfileServiceImpl implements SenderProfileService {

    private final SenderProfileRepository senderProfileRepository;
    private final CampaignRepository campaignRepository;
    private final DomainRepository domainRepository;
    private final SenderProfileMapper senderProfileMapper;
    private final SmtpTestService smtpTestService;
    private final UserCurrentContextService userCurrentContextService;
    private final CatalogReferenceResolver catalogReferenceResolver;
    private final Validator validator;
    private final CredentialEncryptionService credentialEncryptionService;

    private static final String ROLE_CLIENT_ADMIN = UserType.CLIENT_ADMIN.getValue();
    private static final ProviderType DEFAULT_PROVIDER_TYPE = ProviderType.OTHER;
    private static final Set<String> REQUIRED_IMPORT_HEADERS = Set.of(
            "profileName", "interfaceType", "fromAddress", "host", "port"
    );
    private static final Set<String> ALLOWED_IMPORT_HEADERS = Set.of(
            "profileName", "interfaceType", "fromAddress", "displayName", "host", "port", "username", "password",
            "useTls", "ignoreCertificateErrors", "category", "targetIndustryId", "regionId", "language",
            "deceptionLevel", "psychologicalTriggers", "domainType", "domainName", "personalizationLevel",
            "providerType", "tags", "replyToAddress"
    );
    private static final int MAX_IMPORT_ROWS = 5000;

    @Value("${aws.ses.smtp.username:}")
    private String awsSesSmtpUsername;

    @Value("${aws.ses.smtp.password:}")
    private String awsSesSmtpPassword;

    @Override
    public List<SenderProfileDto> getSenderProfiles(int offset, int pageSize, String searchParam,
                                                     String clientId, ProfileType profileType, Boolean isVerified,
                                                     String category, String targetIndustryId, String regionId, String language,
                                                     String deceptionLevelId, List<String> psychologicalTriggers,
                                                     DomainType domainType, String personalizationLevelId,
                                                     ProviderType providerType, List<String> tags,
                                                     String sortBy, String sortOrder) {
        String effectiveClientId = clientId != null && !clientId.trim().isEmpty() ? clientId.trim() : null;
        
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "createdAt"
        );
        
        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int pageNumber = Math.max(0, offset);
        Pageable pageable = PageRequest.of(pageNumber, effectivePageSize, sort);

        String search = searchParam != null && !searchParam.trim().isEmpty() ? searchParam.trim() : null;

        List<SenderProfile> profiles = senderProfileRepository.findWithFilters(
                effectiveClientId,
                search,
                profileType,
                isVerified,
                category,
                targetIndustryId,
                regionId,
                language,
                deceptionLevelId,
                psychologicalTriggers,
                domainType,
                personalizationLevelId,
                providerType,
                tags,
                pageable);

        UserType currentUserType = getCurrentUserType();
        
        return profiles.stream()
                .map(profile -> senderProfileMapper.toDto(profile, canEdit(profile, currentUserType), canDelete(profile, currentUserType)))
                .collect(Collectors.toList());
    }

    @Override
    public long countSenderProfiles(String searchParam, String clientId, ProfileType profileType, Boolean isVerified,
                                    String category, String targetIndustryId, String regionId, String language,
                                    String deceptionLevelId, List<String> psychologicalTriggers, DomainType domainType,
                                    String personalizationLevelId, ProviderType providerType, List<String> tags) {
        String effectiveClientId = clientId != null && !clientId.trim().isEmpty() ? clientId.trim() : null;
        String search = searchParam != null && !searchParam.trim().isEmpty() ? searchParam.trim() : null;
        return senderProfileRepository.countWithFilters(effectiveClientId, search, profileType, isVerified,
                category, targetIndustryId, regionId, language, deceptionLevelId, psychologicalTriggers,
                domainType, personalizationLevelId, providerType, tags);
    }

    @Override
    public SenderProfileDto getSenderProfileById(String profileId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        UserType currentUserType = getCurrentUserType();
        SenderProfile profile = senderProfileRepository.findByIdAndClientIdOrGlobal(profileId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender profile not found"));
        
        return senderProfileMapper.toDto(profile, canEdit(profile, currentUserType), canDelete(profile, currentUserType));
    }

    @Override
    public SenderProfileDto createSenderProfile(SenderProfileRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        String userId = getCurrentUserId();
        UserType userType = getCurrentUserType();
        
        // Check for duplicate name
        if (senderProfileRepository.existsByClientIdAndProfileName(clientId, request.getProfileName())) {
            throw new RuntimeException("Profile name already exists");
        }
        
        // Warn if domain not verified
        if (!isDomainVerified(request.getFromAddress())) {
            log.warn("Creating profile with unverified domain: {}", getDomainFromEmail(request.getFromAddress()));
        }
        
        ProviderType providerType = resolveProviderType(request.getProviderType());
        ResolvedCredentials resolvedCredentials = resolveCredentialsForCreate(
                request.getUsername(), request.getPassword(), providerType);

        // Create entity
        SenderProfile profile = senderProfileMapper.toEntity(request, clientId);
        profile.setDeceptionLevel(catalogReferenceResolver.resolveDeceptionLevel(request.getDeceptionLevel()));
        profile.setPersonalizationLevel(
                catalogReferenceResolver.resolvePersonalizationLevel(request.getPersonalizationLevel()));
        profile.setProviderType(providerType);
        profile.setUsername(resolvedCredentials.username());
        profile.setPassword(encryptPassword(resolvedCredentials.password()));
        profile.setCreatedBy(userId);
        profile.setCreatedByRole(userType != null ? userType.getValue() : null);
        profile.setGlobal(isGlobalProfile(userType));
        profile.setProfileType(isGlobalProfile(userType) ? ProfileType.MANAGED : ProfileType.CUSTOM);
        
        // Save
        SenderProfile saved = senderProfileRepository.save(profile);
        log.info("Created sender profile: {} for client: {}", saved.getId(), clientId);
        
        return senderProfileMapper.toDto(saved, true, true);
    }

    @Override
    public SenderProfileDto updateSenderProfile(String profileId, SenderProfileRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        UserType currentUserType = getCurrentUserType();
        
        SenderProfile profile = senderProfileRepository.findByIdAndClientIdOrGlobal(profileId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender profile not found"));
        
        // Check if editable
        if (!canEdit(profile, currentUserType)) {
            throw new ServiceException("You do not have permission to edit this sender profile");
        }
        
        // Check for duplicate name (excluding current)
        if (senderProfileRepository.existsByClientIdAndProfileNameAndIdNot(
                clientId, request.getProfileName(), profileId)) {
            throw new ServiceException("Profile name already exists");
        }
        
        ProviderType providerType = resolveProviderType(
                request.getProviderType() != null ? request.getProviderType() : profile.getProviderType());
        ResolvedCredentials resolvedCredentials = resolveCredentialsForUpdate(
                request.getUsername(), request.getPassword(), profile, providerType);

        DeceptionLevelDto resolvedDeception = request.getDeceptionLevel() != null
                ? catalogReferenceResolver.resolveDeceptionLevel(request.getDeceptionLevel())
                : profile.getDeceptionLevel();
        PersonalizationLevelDto resolvedPersonalization = request.getPersonalizationLevel() != null
                ? catalogReferenceResolver.resolvePersonalizationLevel(request.getPersonalizationLevel())
                : profile.getPersonalizationLevel();

        // Update entity
        senderProfileMapper.updateFromRequest(profile, request);
        profile.setDeceptionLevel(resolvedDeception);
        profile.setPersonalizationLevel(resolvedPersonalization);
        profile.setProviderType(providerType);
        profile.setUsername(resolvedCredentials.username());
        profile.setPassword(encryptPassword(resolvedCredentials.password()));
        
        // Save
        SenderProfile saved = senderProfileRepository.save(profile);
        log.info("Updated sender profile: {}", profileId);
        
        return senderProfileMapper.toDto(saved, true, true);
    }

    @Override
    public void deleteSenderProfile(String profileId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        UserType currentUserType = getCurrentUserType();

        SenderProfile profile = findProfileForDelete(profileId, clientId, currentUserType);

        // ASPIRE_ADMIN (SYSTEM_ADMIN) / SUPER_ADMIN / SYSTEM_USER, or the profile creator
        if (!canDelete(profile, currentUserType)) {
            throw new ServiceException("You do not have permission to delete this sender profile");
        }

        String campaignClientId = clientId != null && !clientId.isBlank() ? clientId : profile.getClientId();
        if (campaignClientId != null
                && !campaignRepository.findByClientIdAndSenderProfileId(campaignClientId, profileId).isEmpty()) {
            throw new ServiceException("Cannot delete sender profile. It is currently used by active campaigns");
        }

        senderProfileRepository.delete(profile);
        log.info("Deleted sender profile: {}", profileId);
    }

    @Override
    public SenderProfileDto duplicateSenderProfile(String profileId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        UserType userType = getCurrentUserType();
        String userId = getCurrentUserId();
        
        SenderProfile original = senderProfileRepository.findByIdAndClientIdOrGlobal(profileId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender profile not found"));
        
        // Generate unique name
        String baseName = original.getProfileName();
        String newName = baseName + " (Copy)";
        int counter = 1;
        while (senderProfileRepository.existsByClientIdAndProfileName(clientId, newName)) {
            counter++;
            newName = baseName + " (Copy " + counter + ")";
        }
        
        // Create duplicate
        SenderProfile duplicate = senderProfileMapper.createDuplicate(original, newName);
        duplicate.setCreatedBy(userId);
        duplicate.setCreatedByRole(userType != null ? userType.getValue() : null);
        duplicate.setGlobal(false);
        
        // Save
        SenderProfile saved = senderProfileRepository.save(duplicate);
        log.info("Duplicated sender profile: {} -> {}", profileId, saved.getId());
        
        return senderProfileMapper.toDto(saved, true, true);
    }

    @Override
    public TestResultDto testProfileConnection(String profileId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        SenderProfile profile = senderProfileRepository.findByIdAndClientIdOrGlobal(profileId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender profile not found"));
        
        ProviderType providerType = resolveProviderType(profile.getProviderType());
        ResolvedCredentials resolvedCredentials = resolveCredentialsForExistingProfile(profile, providerType);

        // Resolve runtime credentials for testing
        SenderProfile testProfile = SenderProfile.builder()
                .host(profile.getHost())
                .port(profile.getPort())
                .username(resolvedCredentials.username())
                .password(resolvedCredentials.password())
                .useTls(profile.isUseTls())
                .ignoreCertificateErrors(profile.isIgnoreCertificateErrors())
                .build();
        
        // Test connection
        TestResultDto result = smtpTestService.testConnection(testProfile);
        
        // Update profile with test result
        profile.setLastTestedAt(Instant.now());
        profile.setLastTestResult(result.isSuccess() ? "SUCCESS" : "FAILED: " + result.getMessage());
        profile.setVerified(result.isSuccess());
        senderProfileRepository.save(profile);
        
        log.info("Tested sender profile {}: {}", profileId, result.isSuccess() ? "SUCCESS" : "FAILED");
        
        return result;
    }

    @Override
    public TestResultDto testNewConnection(SenderProfileRequest request) {
        ProviderType providerType = resolveProviderType(request.getProviderType());
        ResolvedCredentials resolvedCredentials = resolveCredentialsForCreate(
                request.getUsername(), request.getPassword(), providerType);

        return smtpTestService.testConnection(
                request.getHost(),
                request.getPort(),
                resolvedCredentials.username(),
                resolvedCredentials.password(),
                request.isUseTls(),
                request.isIgnoreCertificateErrors()
        );
    }

    @Override
    public boolean isProfileNameExists(String profileName) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        return senderProfileRepository.existsByClientIdAndProfileName(clientId, profileName);
    }

    @Override
    public List<SenderProfileDto> getVerifiedProfiles() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        UserType currentUserType = getCurrentUserType();
        
        return senderProfileRepository.findByClientIdAndIsVerifiedTrue(clientId).stream()
                .map(profile -> senderProfileMapper.toDto(profile, canEdit(profile, currentUserType), canDelete(profile, currentUserType)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDomainVerified(String fromAddress) {
        String domain = getDomainFromEmail(fromAddress);
        if (domain == null) {
            return false;
        }
        
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        return domainRepository.findByClientIdAndDomainName(clientId, domain)
                .map(d -> "VERIFIED".equals(d.getStatus().name()))
                .orElse(false);
    }

    @Override
    public SenderProfileImportResultDto importSenderProfiles(MultipartFile file) {
        validateImportFile(file);

        List<SenderProfileDto> importedProfiles = new ArrayList<>();
        List<SenderProfileImportErrorDto> errors = new ArrayList<>();
        Set<String> inFileProfileNames = new HashSet<>();
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            validateImportHeaders(parser);

            for (CSVRecord record : parser) {
                totalRows++;
                if (totalRows > MAX_IMPORT_ROWS) {
                    errors.add(SenderProfileImportErrorDto.builder()
                            .rowNumber(toHumanRowNumber(record))
                            .message("Maximum allowed rows exceeded: " + MAX_IMPORT_ROWS)
                            .build());
                    break;
                }

                try {
                    SenderProfileImportRowRequest row = parseImportRow(record);
                    String normalizedName = normalizeForDuplicateCheck(row.getProfileName());
                    validateRowRequest(record, row, normalizedName, inFileProfileNames);

                    SenderProfileRequest request = mapImportRowToCreateRequest(row);
                    SenderProfileDto created = createSenderProfile(request);
                    importedProfiles.add(created);
                } catch (Exception ex) {
                    errors.add(SenderProfileImportErrorDto.builder()
                            .rowNumber(toHumanRowNumber(record))
                            .profileName(getSafe(record, "profileName"))
                            .message(extractImportErrorMessage(ex))
                            .build());
                }
            }
        } catch (IOException ex) {
            throw new ServiceException("Failed to read CSV file");
        }

        return SenderProfileImportResultDto.builder()
                .totalRows(totalRows)
                .successCount(importedProfiles.size())
                .failedCount(errors.size())
                .importedProfiles(importedProfiles)
                .errors(errors)
                .build();
    }

    // --- Helper methods ---

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Import file is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new ServiceException("Only CSV files are supported");
        }
    }

    private void validateImportHeaders(CSVParser parser) {
        Set<String> headers = parser.getHeaderMap().keySet();
        for (String requiredHeader : REQUIRED_IMPORT_HEADERS) {
            if (!headers.contains(requiredHeader)) {
                throw new ServiceException("Missing required CSV header: " + requiredHeader);
            }
        }
        for (String header : headers) {
            if (!ALLOWED_IMPORT_HEADERS.contains(header)) {
                throw new ServiceException("Unsupported CSV header: " + header);
            }
        }
    }

    private SenderProfileImportRowRequest parseImportRow(CSVRecord record) {
        return SenderProfileImportRowRequest.builder()
                .profileName(getSafe(record, "profileName"))
                .interfaceType(getSafe(record, "interfaceType"))
                .fromAddress(getSafe(record, "fromAddress"))
                .displayName(getSafe(record, "displayName"))
                .host(getSafe(record, "host"))
                .port(getSafe(record, "port"))
                .username(getSafe(record, "username"))
                .password(getSafe(record, "password"))
                .useTls(getSafe(record, "useTls"))
                .ignoreCertificateErrors(getSafe(record, "ignoreCertificateErrors"))
                .category(getSafe(record, "category"))
                .targetIndustryId(getSafe(record, "targetIndustryId"))
                .regionId(getSafe(record, "regionId"))
                .language(getSafe(record, "language"))
                .deceptionLevel(getSafe(record, "deceptionLevel"))
                .psychologicalTriggers(getSafe(record, "psychologicalTriggers"))
                .domainType(getSafe(record, "domainType"))
                .domainName(getSafe(record, "domainName"))
                .personalizationLevel(getSafe(record, "personalizationLevel"))
                .providerType(getSafe(record, "providerType"))
                .tags(getSafe(record, "tags"))
                .replyToAddress(getSafe(record, "replyToAddress"))
                .build();
    }

    private void validateRowRequest(CSVRecord record, SenderProfileImportRowRequest row, String normalizedName,
                                    Set<String> inFileProfileNames) {
        if (!hasText(normalizedName)) {
            throw new ServiceException("Profile name is required");
        }
        if (!inFileProfileNames.add(normalizedName)) {
            throw new ServiceException("Duplicate profile name in import file");
        }

        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        if (senderProfileRepository.existsByClientIdAndProfileName(clientId, row.getProfileName().trim())) {
            throw new ServiceException("Profile name already exists");
        }

        parseInterfaceType(row.getInterfaceType());
        parseInteger(row.getPort(), "port");

        SenderProfileRequest validationRequest = mapImportRowToCreateRequest(row);
        Set<ConstraintViolation<SenderProfileRequest>> violations = validator.validate(validationRequest);
        if (!violations.isEmpty()) {
            throw new ServiceException(violations.iterator().next().getMessage());
        }

        if (record.size() == 0) {
            throw new ServiceException("Invalid CSV row");
        }
    }

    private SenderProfileRequest mapImportRowToCreateRequest(SenderProfileImportRowRequest row) {
        ProviderType providerType = parseProviderType(row.getProviderType());
        return SenderProfileRequest.builder()
                .profileName(trimOrNull(row.getProfileName()))
                .interfaceType(parseInterfaceType(row.getInterfaceType()))
                .fromAddress(trimOrNull(row.getFromAddress()))
                .displayName(trimOrNull(row.getDisplayName()))
                .host(trimOrNull(row.getHost()))
                .port(parseInteger(row.getPort(), "port"))
                .username(trimOrNull(row.getUsername()))
                .password(trimOrNull(row.getPassword()))
                .useTls(parseBooleanWithDefault(row.getUseTls(), true))
                .ignoreCertificateErrors(parseBooleanWithDefault(row.getIgnoreCertificateErrors(), false))
                .category(trimOrNull(row.getCategory()))
                .targetIndustryId(trimOrNull(row.getTargetIndustryId()))
                .regionId(trimOrNull(row.getRegionId()))
                .language(trimOrNull(row.getLanguage()))
                .deceptionLevel(toDeceptionLevelRef(row.getDeceptionLevel()))
                .psychologicalTriggers(parsePipeSeparatedList(row.getPsychologicalTriggers()))
                .domainType(parseEnum(row.getDomainType(), DomainType.class, "domainType"))
                .domainName(trimOrNull(row.getDomainName()))
                .personalizationLevel(toPersonalizationLevelRef(row.getPersonalizationLevel()))
                .providerType(providerType)
                .tags(parsePipeSeparatedList(row.getTags()))
                .replyToAddress(trimOrNull(row.getReplyToAddress()))
                .build();
    }

    private ProviderType parseProviderType(String providerType) {
        if (!hasText(providerType)) {
            return DEFAULT_PROVIDER_TYPE;
        }
        return parseEnum(providerType, ProviderType.class, "providerType");
    }

    private InterfaceType parseInterfaceType(String interfaceType) {
        return parseEnum(interfaceType, InterfaceType.class, "interfaceType");
    }

    private <T extends Enum<T>> T parseEnum(String value, Class<T> enumClass, String fieldName) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("Invalid value for " + fieldName + ": " + value);
        }
    }

    private Integer parseInteger(String value, String fieldName) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            throw new ServiceException("Invalid numeric value for " + fieldName + ": " + value);
        }
    }

    private boolean parseBooleanWithDefault(String value, boolean defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private List<String> parsePipeSeparatedList(String rawValue) {
        if (!hasText(rawValue)) {
            return null;
        }
        return Arrays.stream(rawValue.split("\\|"))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toList());
    }

    private String normalizeForDuplicateCheck(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String trimOrNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private DeceptionLevelDto toDeceptionLevelRef(String catalogId) {
        if (!hasText(catalogId)) {
            return null;
        }
        return DeceptionLevelDto.builder().id(catalogId.trim()).build();
    }

    private PersonalizationLevelDto toPersonalizationLevelRef(String catalogId) {
        if (!hasText(catalogId)) {
            return null;
        }
        return PersonalizationLevelDto.builder().id(catalogId.trim()).build();
    }

    private int toHumanRowNumber(CSVRecord record) {
        return (int) record.getRecordNumber() + 1;
    }

    private String getSafe(CSVRecord record, String field) {
        if (!record.isMapped(field)) {
            return null;
        }
        String value = record.get(field);
        return value != null ? value.trim() : null;
    }

    private String extractImportErrorMessage(Exception ex) {
        if (ex instanceof ServiceException && hasText(ex.getMessage())) {
            return ex.getMessage();
        }
        if (hasText(ex.getMessage())) {
            return ex.getMessage();
        }
        return "Failed to import row";
    }

    private boolean canEdit(SenderProfile profile, UserType currentUserType) {
        if (currentUserType == null) {
            return false;
        }
        // ASPIRE_ADMIN (SYSTEM_ADMIN) / SUPER_ADMIN / SYSTEM_USER can edit/delete all sender profiles
        if (isPlatformAdmin(currentUserType)) {
            return true;
        }
        // Managed (global) profiles are read-only for non-admin roles
        if (profile.getProfileType() == ProfileType.MANAGED) {
            return false;
        }
        if (currentUserType == UserType.CLIENT_ADMIN) {
            return !profile.isGlobal()
                    && ROLE_CLIENT_ADMIN.equalsIgnoreCase(profile.getCreatedByRole())
                    && getCurrentUserId().equals(profile.getCreatedBy());
        }
        return false;
    }

    /**
     * Delete allowed for platform admins (any profile) and for the profile creator (own non-managed).
     */
    private boolean canDelete(SenderProfile profile, UserType currentUserType) {
        if (currentUserType == null) {
            return false;
        }
        if (isPlatformAdmin(currentUserType)) {
            return true;
        }
        if (profile.getProfileType() == ProfileType.MANAGED) {
            return false;
        }
        String currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(profile.getCreatedBy());
    }

    private boolean isPlatformAdmin(UserType userType) {
        return userType == UserType.SUPER_ADMIN
                || userType == UserType.ASPIRE_ADMIN
                || userType == UserType.SYSTEM_USER;
    }

    private SenderProfile findProfileForDelete(String profileId, String clientId, UserType currentUserType) {
        Optional<SenderProfile> scoped = senderProfileRepository.findByIdAndClientIdOrGlobal(profileId, clientId);
        if (scoped.isPresent()) {
            return scoped.get();
        }
        // Platform admins can delete any profile by id (including non-global tenant profiles)
        if (isPlatformAdmin(currentUserType)) {
            return senderProfileRepository.findById(profileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sender profile not found"));
        }
        throw new ResourceNotFoundException("Sender profile not found");
    }

    private UserType getCurrentUserType() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        try {
            return UserType.fromString(context.getUserType());
        } catch (Exception e) {
            log.warn("Unable to parse userType from context: {}", context.getUserType());
            return null;
        }
    }

    private String getCurrentUserId() {
        return userCurrentContextService.getCurrentUserContext().getUserId();
    }

    private boolean isGlobalProfile(UserType userType) {
        return isPlatformAdmin(userType);
    }

    private String getDomainFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        return email.substring(email.indexOf("@") + 1);
    }

    private String encryptPassword(String password) {
        return credentialEncryptionService.encrypt(password);
    }

    private String decryptPassword(String encryptedPassword) {
        return credentialEncryptionService.decrypt(encryptedPassword);
    }

    private ProviderType resolveProviderType(ProviderType providerType) {
        return providerType != null ? providerType : DEFAULT_PROVIDER_TYPE;
    }

    private ResolvedCredentials resolveCredentialsForCreate(String requestUsername, String requestPassword,
                                                            ProviderType providerType) {
        if (providerType == ProviderType.AWS_SES) {
            return resolveAwsSesCredentials();
        }
        if (!hasText(requestUsername) || !hasText(requestPassword)) {
            throw new ServiceException("Username and password are required for OTHER provider type");
        }
        return new ResolvedCredentials(requestUsername.trim(), requestPassword);
    }

    private ResolvedCredentials resolveCredentialsForUpdate(String requestUsername, String requestPassword,
                                                            SenderProfile existingProfile, ProviderType providerType) {
        if (providerType == ProviderType.AWS_SES) {
            return resolveAwsSesCredentials();
        }

        String resolvedUsername = hasText(requestUsername) ? requestUsername.trim() : existingProfile.getUsername();
        String resolvedPassword = hasText(requestPassword) ? requestPassword : decryptPassword(existingProfile.getPassword());
        if (!hasText(resolvedUsername) || !hasText(resolvedPassword)) {
            throw new ServiceException("Username and password are required for OTHER provider type");
        }
        return new ResolvedCredentials(resolvedUsername, resolvedPassword);
    }

    private ResolvedCredentials resolveCredentialsForExistingProfile(SenderProfile profile, ProviderType providerType) {
        if (providerType == ProviderType.AWS_SES) {
            return resolveAwsSesCredentials();
        }
        String resolvedUsername = profile.getUsername();
        String resolvedPassword = decryptPassword(profile.getPassword());
        if (!hasText(resolvedUsername) || !hasText(resolvedPassword)) {
            throw new ServiceException("Stored username/password are missing for OTHER provider type profile");
        }
        return new ResolvedCredentials(resolvedUsername, resolvedPassword);
    }

    private ResolvedCredentials resolveAwsSesCredentials() {
        if (!hasText(awsSesSmtpUsername) || !hasText(awsSesSmtpPassword)) {
            throw new ServiceException("AWS SES SMTP credentials are not configured");
        }
        return new ResolvedCredentials(awsSesSmtpUsername.trim(), awsSesSmtpPassword);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private record ResolvedCredentials(String username, String password) {
    }
}
