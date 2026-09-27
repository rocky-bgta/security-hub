package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient.UserDto;
import com.aspire.asat.phishing.dto.request.CampaignAudienceRequest;
import com.aspire.asat.phishing.model.PhishingUserLicence;
import com.aspire.asat.phishing.repository.PhishingUserLicenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves users for a campaign audience selection.
 * {@link #resolve} uses Registration (allocate-licence).
 * {@link #resolveLicensed} uses phishing_user_licence (Step 6).
 */
@Component
@RequiredArgsConstructor
public class CampaignAudienceUserResolver {

    private final RegistrationServiceClient registrationClient;
    private final PhishingUserLicenceRepository phishingUserLicenceRepository;

    /**
     * Resolves users from Registration for the given audience request, deduplicated by userId
     * (first occurrence kept). Blank userIds are skipped.
     * Used by allocate-licence (first campaign / new seat selection).
     */
    public List<UserDto> resolve(String clientId, CampaignAudienceRequest request) {
        if (request == null || request.getAudienceType() == null) {
            return List.of();
        }

        List<UserDto> users = new ArrayList<>();
        switch (request.getAudienceType()) {
            case ALL_USERS:
                users = registrationClient.getAllUsers(clientId);
                break;
            case DEPARTMENTS:
                if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
                    users = registrationClient.getUsersByDepartments(clientId, request.getDepartmentIds());
                }
                break;
            case GROUPS:
                if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
                    users = registrationClient.getUsersByGroups(clientId, request.getGroupIds());
                }
                break;
            case INDIVIDUAL:
                if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
                    users = registrationClient.getUsersByIds(clientId, request.getUserIds());
                }
                break;
            default:
                break;
        }

        return dedupeByUserId(users);
    }

    /**
     * Resolves active licensed users from {@code phishing_user_licence} for Step 6 audience.
     * Scoped to clientAdminId + productPackageId. Unlicensed selections are dropped.
     */
    public List<UserDto> resolveLicensed(
            String clientId, String productPackageId, CampaignAudienceRequest request) {
        if (request == null || request.getAudienceType() == null
                || !StringUtils.hasText(clientId) || !StringUtils.hasText(productPackageId)) {
            return List.of();
        }

        List<String> departmentIds = null;
        List<String> groupIds = null;
        List<String> userIds = null;

        switch (request.getAudienceType()) {
            case ALL_USERS:
                break;
            case DEPARTMENTS:
                if (request.getDepartmentIds() == null || request.getDepartmentIds().isEmpty()) {
                    return List.of();
                }
                departmentIds = request.getDepartmentIds();
                break;
            case GROUPS:
                if (request.getGroupIds() == null || request.getGroupIds().isEmpty()) {
                    return List.of();
                }
                groupIds = request.getGroupIds();
                break;
            case INDIVIDUAL:
                if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
                    return List.of();
                }
                userIds = request.getUserIds();
                break;
            default:
                return List.of();
        }

        List<PhishingUserLicence> licences = phishingUserLicenceRepository.findAudienceLicences(
                clientId.trim(), productPackageId.trim(), departmentIds, groupIds, userIds);
        return dedupeByUserId(licences.stream().map(CampaignAudienceUserResolver::toUserDto).toList());
    }

    private static UserDto toUserDto(PhishingUserLicence licence) {
        return UserDto.builder()
                .userId(licence.getUserId())
                .email(licence.getEmail())
                .firstName(licence.getFirstName())
                .lastName(licence.getLastName())
                .departmentId(licence.getDepartmentId())
                .departmentName(licence.getDepartmentName())
                .organizationName(licence.getOrganizationName())
                .organizationDomain(licence.getOrganizationDomain())
                .phoneNumber(licence.getPhoneNumber())
                .countryName(licence.getCountryName())
                .groupIds(licence.getGroupIds() != null ? new ArrayList<>(licence.getGroupIds()) : new ArrayList<>())
                .riskGroup(licence.getRiskGroup())
                .active(licence.isActive())
                .isRiskProfileExist(licence.getIsRiskProfileExist())
                .build();
    }

    private static List<UserDto> dedupeByUserId(List<UserDto> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        Map<String, UserDto> byId = new LinkedHashMap<>();
        for (UserDto user : users) {
            if (user == null || !StringUtils.hasText(user.getUserId())) {
                continue;
            }
            byId.putIfAbsent(user.getUserId().trim(), user);
        }
        return new ArrayList<>(byId.values());
    }
}
