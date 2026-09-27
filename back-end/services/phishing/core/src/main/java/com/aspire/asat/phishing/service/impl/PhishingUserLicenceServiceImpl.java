package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient.ClientProductDetailDto;
import com.aspire.asat.phishing.client.RegistrationServiceClient.UserDto;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.request.AllocateLicenceRequest;
import com.aspire.asat.phishing.dto.request.CampaignAudienceRequest;
import com.aspire.asat.phishing.dto.response.AllocateLicenceResponseDto;
import com.aspire.asat.phishing.dto.response.LicensedUserDepartmentCountDto;
import com.aspire.asat.phishing.dto.response.LicensedUserDto;
import com.aspire.asat.phishing.dto.response.LicensedUserGroupCountDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.PhishingUserLicence;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.PhishingUserLicenceRepository;
import com.aspire.asat.phishing.service.PhishingUserLicenceService;
import com.aspire.asat.phishing.service.support.CampaignAudienceUserResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhishingUserLicenceServiceImpl implements PhishingUserLicenceService {

    private final CampaignRepository campaignRepository;
    private final PhishingUserLicenceRepository phishingUserLicenceRepository;
    private final CampaignAudienceUserResolver campaignAudienceUserResolver;
    private final RegistrationServiceClient registrationServiceClient;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public AllocateLicenceResponseDto allocateLicence(String campaignId, AllocateLicenceRequest request) {
        String clientAdminId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        if (!campaign.isEditable()) {
            throw new PhishingValidationException("License allocation is only allowed for draft campaigns");
        }
        if (!StringUtils.hasText(campaign.getProductPackageId())) {
            throw new PhishingValidationException(
                    "productPackageId is required on the campaign before license allocation");
        }

        String productPackageId = campaign.getProductPackageId().trim();
        ClientProductDetailDto clientProduct = loadAndValidateClientProduct(productPackageId, clientAdminId);

        CampaignAudienceRequest audienceRequest = CampaignAudienceRequest.builder()
                .audienceType(request.getAudienceType())
                .departmentIds(request.getDepartmentIds())
                .groupIds(request.getGroupIds())
                .userIds(request.getUserIds())
                .build();

        List<UserDto> selectedUsers = campaignAudienceUserResolver.resolve(clientAdminId, audienceRequest);
        if (selectedUsers.isEmpty()) {
            throw new PhishingValidationException("Campaign must have at least one recipient for license allocation");
        }

        List<String> selectedUserIds = selectedUsers.stream()
                .map(UserDto::getUserId)
                .collect(Collectors.toList());

        Set<String> alreadyLicensedIds = phishingUserLicenceRepository
                .findByClientAdminIdAndProductPackageIdAndUserIdIn(
                        clientAdminId, productPackageId, selectedUserIds)
                .stream()
                .map(PhishingUserLicence::getUserId)
                .collect(Collectors.toCollection(HashSet::new));

        List<UserDto> existingSelected = new ArrayList<>();
        List<UserDto> newSelected = new ArrayList<>();
        for (UserDto user : selectedUsers) {
            if (alreadyLicensedIds.contains(user.getUserId())) {
                existingSelected.add(user);
            } else {
                newSelected.add(user);
            }
        }

        int licenseCount = clientProduct.getLicenseCount();
        long alreadyUsed = phishingUserLicenceRepository
                .countByClientAdminIdAndProductPackageId(clientAdminId, productPackageId);
        int remaining = Math.max(0, licenseCount - (int) alreadyUsed);

        int newRequired = newSelected.size();
        int existingCount = existingSelected.size();
        int selectedCount = selectedUsers.size();

        boolean overLimit = newRequired > remaining;

        List<UserDto> newToAllocate;
        boolean requiresConfirmation = false;
        String confirmationMessage = null;

        if (overLimit) {
            if (!request.isConfirm()) {
                List<String> proposedUserIds = buildProceedUserIds(existingSelected, newSelected, remaining);
                return AllocateLicenceResponseDto.builder()
                        .licenseCount(licenseCount)
                        .usedLicenseCount((int) alreadyUsed)
                        .availableLicenseCount(remaining)
                        .selectedUserCount(selectedCount)
                        .existingLicensedUserCount(existingCount)
                        .newLicenseRequiredCount(newRequired)
                        .newLicenseAllocatedCount(0)
                        .requiresConfirmation(true)
                        .confirmationMessage(overLimitConfirmationMessage(
                                selectedCount, remaining, proposedUserIds.size()))
                        .userIds(proposedUserIds)
                        .build();
            }
            requiresConfirmation = false;
            newToAllocate = newSelected.subList(0, Math.min(remaining, newSelected.size()));
        } else {
            newToAllocate = newSelected;
        }

        if (!newToAllocate.isEmpty()) {
            List<PhishingUserLicence> rows = newToAllocate.stream()
                    .map(user -> toEntity(user, clientAdminId, productPackageId, clientProduct))
                    .collect(Collectors.toList());
            phishingUserLicenceRepository.saveAll(rows);
            log.info("Allocated {} new phishing licenses for clientAdminId={}, productPackageId={}",
                    rows.size(), clientAdminId, productPackageId);
        }

        int allocated = newToAllocate.size();
        int usedAfter = (int) alreadyUsed + allocated;
        syncUsedLicenseCountToRegistration(productPackageId, usedAfter);
        List<String> proceedUserIds = buildProceedUserIds(existingSelected, newToAllocate, allocated);

        return AllocateLicenceResponseDto.builder()
                .licenseCount(licenseCount)
                .usedLicenseCount(usedAfter)
                .availableLicenseCount(Math.max(0, licenseCount - usedAfter))
                .selectedUserCount(selectedCount)
                .existingLicensedUserCount(existingCount)
                .newLicenseRequiredCount(newRequired)
                .newLicenseAllocatedCount(allocated)
                .requiresConfirmation(requiresConfirmation)
                .confirmationMessage(confirmationMessage)
                .userIds(proceedUserIds)
                .build();
    }

    private void syncUsedLicenseCountToRegistration(String productPackageId, int usedLicenseCount) {
        try {
            registrationServiceClient.updateClientProductUsedLicenseCount(productPackageId, usedLicenseCount);
        } catch (Exception e) {
            log.error("Failed to sync usedLicenseCount={} for productPackageId={}: {}",
                    usedLicenseCount, productPackageId, e.getMessage(), e);
            throw new ServiceException(
                    "License seats were allocated but used license count could not be synced. Please retry.",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    e);
        }
    }

    @Override
    public AllResponseDto<List<LicensedUserDto>> listLicensedUsers(
            String campaignId,
            String search,
            List<String> departments,
            List<RiskGroup> riskGroups,
            int offset,
            int pageSize) {
        CampaignPackageScope scope = resolveCampaignPackage(campaignId);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int safeOffset = Math.max(offset, 0);

        List<LicensedUserDto> items = phishingUserLicenceRepository
                .findLicensedUsers(
                        scope.clientAdminId(),
                        scope.productPackageId(),
                        search,
                        departments,
                        riskGroups,
                        safeOffset,
                        safePageSize)
                .stream()
                .map(PhishingUserLicenceServiceImpl::toLicensedUserDto)
                .collect(Collectors.toList());

        long total = phishingUserLicenceRepository.countLicensedUsers(
                scope.clientAdminId(),
                scope.productPackageId(),
                search,
                departments,
                riskGroups);

        return AllResponseDto.<List<LicensedUserDto>>builder()
                .items(items)
                .total(total)
                .offset(safeOffset)
                .pageSize(safePageSize)
                .build();
    }

    @Override
    public List<LicensedUserDepartmentCountDto> getLicensedUserDepartmentCounts(String campaignId) {
        CampaignPackageScope scope = resolveCampaignPackage(campaignId);
        return phishingUserLicenceRepository.countByDepartment(
                scope.clientAdminId(), scope.productPackageId());
    }

    @Override
    public List<LicensedUserGroupCountDto> getLicensedUserGroupCounts(String campaignId) {
        CampaignPackageScope scope = resolveCampaignPackage(campaignId);
        return phishingUserLicenceRepository.countByGroup(
                scope.clientAdminId(), scope.productPackageId());
    }

    private CampaignPackageScope resolveCampaignPackage(String campaignId) {
        String clientAdminId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientAdminId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
        if (!StringUtils.hasText(campaign.getProductPackageId())) {
            throw new PhishingValidationException(
                    "productPackageId is required on the campaign before listing licensed users");
        }
        return new CampaignPackageScope(clientAdminId, campaign.getProductPackageId().trim());
    }

    private static LicensedUserDto toLicensedUserDto(PhishingUserLicence licence) {
        String firstName = licence.getFirstName();
        String lastName = licence.getLastName();
        String fullName;
        if (StringUtils.hasText(firstName) && StringUtils.hasText(lastName)) {
            fullName = firstName.trim() + " " + lastName.trim();
        } else if (StringUtils.hasText(firstName)) {
            fullName = firstName.trim();
        } else if (StringUtils.hasText(lastName)) {
            fullName = lastName.trim();
        } else {
            fullName = null;
        }

        return LicensedUserDto.builder()
                .id(licence.getUserId())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName)
                .email(licence.getEmail())
                .phoneNumber(licence.getPhoneNumber())
                .department(licence.getDepartmentName())
                .organizationName(licence.getOrganizationName())
                .organizationDomain(licence.getOrganizationDomain())
                .countryName(licence.getCountryName())
                .status(licence.isActive() ? "ACTIVE" : "INACTIVE")
                .clientAdminId(licence.getClientAdminId())
                .isRiskProfileExist(licence.getIsRiskProfileExist())
                .riskGroup(licence.getRiskGroup())
                .build();
    }

    private record CampaignPackageScope(String clientAdminId, String productPackageId) {
    }

    private ClientProductDetailDto loadAndValidateClientProduct(String productPackageId, String clientAdminId) {
        ClientProductDetailDto detail = registrationServiceClient.getProductPackageDetail(productPackageId);
        if (detail == null || !StringUtils.hasText(detail.getId())) {
            throw new ResourceNotFoundException("Product package not found: " + productPackageId);
        }
        if (!clientAdminId.equals(detail.getClientAdminId())) {
            throw new PhishingValidationException("Product package does not belong to the current client");
        }
        if (!"ACTIVE".equalsIgnoreCase(detail.getLicenseStatus())) {
            throw new PhishingValidationException("Product package license is not ACTIVE");
        }
        if (detail.getExpiryDate() != null && detail.getExpiryDate().isBefore(Instant.now())) {
            throw new PhishingValidationException("Product package license has expired");
        }
        if (detail.getLicenseCount() <= 0) {
            throw new PhishingValidationException("Product package has no licenses");
        }
        return detail;
    }

    private static String overLimitConfirmationMessage(
            int selectedCount, int availableCount, int proceedCount) {
        int skippedCount = selectedCount - proceedCount;
        return String.format(
                "License Limit Reached\n"
                        + "You selected %d users, but only %d licenses are available.\n"
                        + "The campaign will be sent to the first %d users in your selected list. "
                        + "The remaining %d users will be skipped.",
                selectedCount, availableCount, proceedCount, skippedCount);
    }

    private static List<String> buildProceedUserIds(
            List<UserDto> existingSelected, List<UserDto> newUsers, int newLimit) {
        List<String> ids = new ArrayList<>();
        for (UserDto user : existingSelected) {
            ids.add(user.getUserId());
        }
        int limit = Math.min(newLimit, newUsers.size());
        for (int i = 0; i < limit; i++) {
            ids.add(newUsers.get(i).getUserId());
        }
        return ids;
    }

    private static PhishingUserLicence toEntity(
            UserDto user, String clientAdminId, String productPackageId, ClientProductDetailDto product) {
        return PhishingUserLicence.builder()
                .productPackageId(productPackageId)
                .clientAdminId(clientAdminId)
                .packageExpireDate(product.getExpiryDate())
                .licenceCount(product.getLicenseCount())
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .departmentId(user.getDepartmentId())
                .departmentName(user.getDepartmentName())
                .organizationName(user.getOrganizationName())
                .organizationDomain(user.getOrganizationDomain())
                .phoneNumber(user.getPhoneNumber())
                .countryName(user.getCountryName())
                .groupIds(user.getGroupIds() != null ? new ArrayList<>(user.getGroupIds()) : new ArrayList<>())
                .active(user.isActive())
                .isRiskProfileExist(user.getIsRiskProfileExist())
                .riskGroup(user.getRiskGroup())
                .build();
    }
}
