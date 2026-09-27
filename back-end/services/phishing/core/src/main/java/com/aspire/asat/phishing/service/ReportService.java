package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.response.*;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for report operations.
 */
public interface ReportService {

    /**
     * Get campaign reports list
     */
    List<CampaignPerformanceDto> getCampaignReports(int offset, int pageSize, 
            String sortBy, String sortOrder);

    List<CampaignPerformanceDto> getCampaignReports(int offset, int pageSize, 
            String sortBy, String sortOrder, CampaignChannel channel);

    /**
     * Count campaign reports
     */
    long countCampaignReports();

    long countCampaignReports(CampaignChannel channel);

    /**
     * Get detailed campaign report
     */
    CampaignPerformanceDto getCampaignReportById(String campaignId);

    /**
     * Get email activity log
     */
    List<EmailActivityDto> getEmailActivityLog(int offset, int pageSize, 
            ActivityType activityType, Instant startTime, Instant endTime, String search,
            CampaignChannel channel);

    /**
     * Count email activities
     */
    long countEmailActivities(ActivityType activityType, Instant startTime, Instant endTime, String search,
            CampaignChannel channel);

    /**
     * Get user risk report
     */
    List<UserRiskSummaryDto> getUserRiskReport(int offset, int pageSize, 
            String department, String search, RiskLevel riskLevel, String sortBy, String sortOrder);

    List<UserRiskSummaryDto> getUserRiskReport(int offset, int pageSize, 
            String department, String search, RiskLevel riskLevel, String sortBy, String sortOrder,
            CampaignChannel channel);

    /**
     * Count users for risk report
     */
    long countUsersForRiskReport(String department, String search, RiskLevel riskLevel);

    long countUsersForRiskReport(String department, String search, RiskLevel riskLevel, CampaignChannel channel);

    /**
     * Export campaign report to byte array (PDF or Excel)
     */
    byte[] exportCampaignReport(String campaignId, String format);

    /**
     * Export user risk report to byte array (PDF or Excel)
     */
    byte[] exportUserRiskReport(String format);

    byte[] exportUserRiskReport(String format, CampaignChannel channel);
}
