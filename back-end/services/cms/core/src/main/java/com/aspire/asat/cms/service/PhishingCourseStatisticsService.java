package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseEnrollmentDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseStatisticsCountsDto;

import java.util.Collection;
import java.util.List;

public interface PhishingCourseStatisticsService {

    PhishingCourseStatisticsCountsDto getStatistics(String clientAdminId);

    PhishingCourseStatisticsCountsDto getStatistics(String clientAdminId, Collection<String> subPackageIds);

    AllResponseDto<List<PhishingCourseEnrollmentDto>> getDetails(
            String clientAdminId, int offset, int pageSize, Collection<String> userIds);
}
