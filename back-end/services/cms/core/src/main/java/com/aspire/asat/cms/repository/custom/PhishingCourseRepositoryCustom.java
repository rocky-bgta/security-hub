package com.aspire.asat.cms.repository.custom;

import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseEnrollmentDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseStatisticsCountsDto;

import java.util.Collection;
import java.util.List;

public interface PhishingCourseRepositoryCustom {

    PhishingCourseStatisticsCountsDto getStatisticsCounts(String clientAdminId);

    PhishingCourseStatisticsCountsDto getStatisticsCounts(String clientAdminId, Collection<String> subPackageIds);

    List<PhishingCourseEnrollmentDto> getDetails(String clientAdminId, int offset, int pageSize, Collection<String> userIds);

    long countDetails(String clientAdminId, Collection<String> userIds);
}
