package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.client.CmsPhishingCourseClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient.UserDto;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCourseEnrollmentDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCoursePageDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCourseStatisticsCountsDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.PhishingCourseDetailDto;
import com.aspire.asat.phishing.dto.response.PhishingCourseStatisticsDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.service.PhishingCourseService;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhishingCourseServiceImpl implements PhishingCourseService {

    private final CmsPhishingCourseClient cmsPhishingCourseClient;
    private final RegistrationServiceClient registrationServiceClient;
    private final CampaignRepository campaignRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public PhishingCourseStatisticsDto getStatistics() {
        return getStatistics(CampaignChannel.EMAIL);
    }

    @Override
    public PhishingCourseStatisticsDto getStatistics(CampaignChannel channel) {
        String clientAdminId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        log.info("Getting phishing course statistics for clientAdminId={}, channel={}",
                clientAdminId, effectiveChannel);

        List<String> moduleIds = campaignRepository.findTrainingModuleIdsByClientIdAndChannel(
                clientAdminId, effectiveChannel);
        if (moduleIds.isEmpty()) {
            return emptyStatistics();
        }

        CmsPhishingCourseStatisticsCountsDto counts =
                cmsPhishingCourseClient.getStatistics(clientAdminId, moduleIds);
        long total = counts.getTotalUsers();

        return PhishingCourseStatisticsDto.builder()
                .totalUsers(total)
                .completedUsers(counts.getCompletedUsers())
                .completePercentage(percentage(counts.getCompletedUsers(), total))
                .inProgressUsers(counts.getInProgressUsers())
                .inProgressPercentage(percentage(counts.getInProgressUsers(), total))
                .pendingUsers(counts.getPendingUsers())
                .pendingPercentage(percentage(counts.getPendingUsers(), total))
                .expiredUsers(counts.getExpiredUsers())
                .expiredPercentage(percentage(counts.getExpiredUsers(), total))
                .build();
    }

    private static PhishingCourseStatisticsDto emptyStatistics() {
        return PhishingCourseStatisticsDto.builder()
                .totalUsers(0)
                .completedUsers(0)
                .completePercentage(0)
                .inProgressUsers(0)
                .inProgressPercentage(0)
                .pendingUsers(0)
                .pendingPercentage(0)
                .expiredUsers(0)
                .expiredPercentage(0)
                .build();
    }

    @Override
    public AllResponseDto<List<PhishingCourseDetailDto>> getDetails(int offset, int pageSize, String email) {
        String clientAdminId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        log.info("Getting phishing course details for clientAdminId={}, offset={}, pageSize={}, email={}",
                clientAdminId, offset, pageSize, email);

        boolean hasEmailFilter = email != null && !email.isBlank();
        Map<String, UserDto> prefetchedUsers = null;
        List<String> filterUserIds = null;

        if (hasEmailFilter) {
            prefetchedUsers = fetchUsersMatchingEmail(clientAdminId, email);
            if (prefetchedUsers.isEmpty()) {
                log.info("No users matched email filter '{}' for clientAdminId={}, returning empty page",
                        email, clientAdminId);
                return emptyPage(offset, pageSize);
            }
            filterUserIds = List.copyOf(prefetchedUsers.keySet());
        }

        CmsPhishingCoursePageDto page = cmsPhishingCourseClient.getDetails(clientAdminId, offset, pageSize, filterUserIds);
        List<CmsPhishingCourseEnrollmentDto> enrollments = page.getItems() == null
                ? Collections.emptyList()
                : page.getItems();

        Map<String, UserDto> usersById = prefetchedUsers != null
                ? prefetchedUsers
                : fetchUsersByEnrollmentIds(clientAdminId, enrollments);
        Map<String, String> campaignNamesBySubPackageId = fetchCampaignNames(clientAdminId, enrollments);

        List<PhishingCourseDetailDto> items = enrollments.stream()
                .map(enrollment -> toDetailDto(enrollment, usersById, campaignNamesBySubPackageId))
                .toList();

        long total = page.getTotal() != null ? page.getTotal() : items.size();
        return AllResponseDto.<List<PhishingCourseDetailDto>>builder()
                .offset(offset)
                .pageSize(pageSize)
                .total(total)
                .items(items)
                .build();
    }

    private Map<String, UserDto> fetchUsersMatchingEmail(String clientAdminId, String email) {
        String needle = email.trim().toLowerCase();
        try {
            List<UserDto> allUsers = registrationServiceClient.getAllUsers(clientAdminId);
            Map<String, UserDto> matching = new LinkedHashMap<>();
            for (UserDto user : allUsers) {
                if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
                    continue;
                }
                if (user.getEmail() != null && user.getEmail().toLowerCase().contains(needle)) {
                    matching.put(user.getUserId(), user);
                }
            }
            return matching;
        } catch (Exception e) {
            log.error("Failed to resolve users by email '{}' for clientAdminId={}: {}",
                    email, clientAdminId, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private Map<String, UserDto> fetchUsersByEnrollmentIds(String clientAdminId, List<CmsPhishingCourseEnrollmentDto> enrollments) {
        Set<String> userIds = enrollments.stream()
                .map(CmsPhishingCourseEnrollmentDto::getUserId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<UserDto> users = registrationServiceClient.getUsersByIds(clientAdminId, List.copyOf(userIds));
            Map<String, UserDto> result = new HashMap<>();
            for (UserDto user : users) {
                if (user != null && user.getUserId() != null) {
                    result.put(user.getUserId(), user);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch users from registration for clientAdminId={}: {}", clientAdminId, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private static AllResponseDto<List<PhishingCourseDetailDto>> emptyPage(int offset, int pageSize) {
        return AllResponseDto.<List<PhishingCourseDetailDto>>builder()
                .offset(offset)
                .pageSize(pageSize)
                .total(0L)
                .items(List.of())
                .build();
    }

    private Map<String, String> fetchCampaignNames(String clientAdminId, List<CmsPhishingCourseEnrollmentDto> enrollments) {
        Set<String> subPackageIds = enrollments.stream()
                .map(CmsPhishingCourseEnrollmentDto::getSubPackageId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (subPackageIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<Campaign> campaigns = campaignRepository.findByClientIdAndTrainingModuleIdIn(clientAdminId, subPackageIds);
            Map<String, String> result = new HashMap<>();
            for (Campaign campaign : campaigns) {
                if (campaign.getTrainingData() == null) {
                    continue;
                }
                String moduleId = campaign.getTrainingData().getTrainingModuleId();
                if (moduleId == null || moduleId.isBlank()) {
                    continue;
                }
                result.putIfAbsent(moduleId, campaign.getCampaignName());
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch campaigns for clientAdminId={}: {}", clientAdminId, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private PhishingCourseDetailDto toDetailDto(
            CmsPhishingCourseEnrollmentDto enrollment,
            Map<String, UserDto> usersById,
            Map<String, String> campaignNamesBySubPackageId) {
        UserDto user = enrollment.getUserId() != null ? usersById.get(enrollment.getUserId()) : null;

        String campaignName = enrollment.getSubPackageId() != null
                ? campaignNamesBySubPackageId.get(enrollment.getSubPackageId())
                : null;
        if (campaignName == null || campaignName.isBlank()) {
            campaignName = enrollment.getSubPackageName();
        }

        return PhishingCourseDetailDto.builder()
                .campaignName(campaignName)
                .userName(buildFullName(user))
                .email(user != null ? user.getEmail() : null)
                .department(user != null ? user.getDepartmentName() : null)
                .assignedDate(enrollment.getAssignedDate())
                .expireDate(enrollment.getExpiryDate())
                .status(enrollment.getStatus())
                .progress(enrollment.getProgress())
                .build();
    }

    private static String buildFullName(UserDto user) {
        if (user == null) {
            return null;
        }
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? null : combined;
    }

    private static long percentage(long value, long total) {
        if (total <= 0) {
            return 0L;
        }
        return Math.round((value * 100.0) / total);
    }
}
