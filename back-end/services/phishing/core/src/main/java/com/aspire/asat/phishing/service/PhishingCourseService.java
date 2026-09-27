package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.PhishingCourseDetailDto;
import com.aspire.asat.phishing.dto.response.PhishingCourseStatisticsDto;

import java.util.List;

public interface PhishingCourseService {

    PhishingCourseStatisticsDto getStatistics();

    PhishingCourseStatisticsDto getStatistics(CampaignChannel channel);

    AllResponseDto<List<PhishingCourseDetailDto>> getDetails(int offset, int pageSize, String email);
}
