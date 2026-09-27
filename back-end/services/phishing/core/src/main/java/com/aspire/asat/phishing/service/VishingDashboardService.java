package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.SuccessKeywordsRequest;
import com.aspire.asat.phishing.dto.request.TeachableMomentRequest;
import com.aspire.asat.phishing.dto.request.VoiceTestCallRequest;
import com.aspire.asat.phishing.dto.response.*;

import java.util.List;

public interface VishingDashboardService {

    VishingDataCaptureDto getDataCapture(String campaignId, int offset, int pageSize);

    List<String> getSuccessKeywords(String campaignId);

    void updateSuccessKeywords(String campaignId, SuccessKeywordsRequest request);

    VishingRemediationDto getRemediation(String campaignId);

    void sendTeachableMoment(String campaignId, TeachableMomentRequest request);

    VishingReportDto getReport(String campaignId);

    byte[] exportReport(String campaignId, String format, boolean anonymize);

    VoiceLiveMetricsDto getLiveMetrics(String campaignId);

    void sendTestCall(String campaignId, VoiceTestCallRequest request);
}
