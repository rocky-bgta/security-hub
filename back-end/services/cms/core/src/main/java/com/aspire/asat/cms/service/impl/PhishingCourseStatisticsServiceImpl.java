package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseEnrollmentDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseStatisticsCountsDto;
import com.aspire.asat.cms.repository.custom.PhishingCourseRepositoryCustom;
import com.aspire.asat.cms.service.PhishingCourseStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhishingCourseStatisticsServiceImpl implements PhishingCourseStatisticsService {

    private final PhishingCourseRepositoryCustom phishingCourseRepository;

    @Override
    public PhishingCourseStatisticsCountsDto getStatistics(String clientAdminId) {
        return getStatistics(clientAdminId, null);
    }

    @Override
    public PhishingCourseStatisticsCountsDto getStatistics(String clientAdminId, Collection<String> subPackageIds) {
        log.info("Fetching phishing course statistics for clientAdminId={}, subPackageIdsCount={}",
                clientAdminId, subPackageIds == null ? 0 : subPackageIds.size());
        PhishingCourseStatisticsCountsDto counts =
                phishingCourseRepository.getStatisticsCounts(clientAdminId, subPackageIds);
        log.info("Phishing course statistics for clientAdminId={}: total={}, completed={}, inProgress={}, pending={}, expired={}",
                clientAdminId,
                counts.getTotalUsers(),
                counts.getCompletedUsers(),
                counts.getInProgressUsers(),
                counts.getPendingUsers(),
                counts.getExpiredUsers());
        return counts;
    }

    @Override
    public AllResponseDto<List<PhishingCourseEnrollmentDto>> getDetails(
            String clientAdminId, int offset, int pageSize, Collection<String> userIds) {
        int userIdCount = userIds == null ? 0 : userIds.size();
        log.info("Fetching phishing course details for clientAdminId={}, offset={}, pageSize={}, userIdsCount={}",
                clientAdminId, offset, pageSize, userIdCount);

        List<PhishingCourseEnrollmentDto> items = phishingCourseRepository.getDetails(
                clientAdminId, offset, pageSize, userIds);
        long total = phishingCourseRepository.countDetails(clientAdminId, userIds);

        return new AllResponseDto<>(offset, pageSize, total, items);
    }
}
