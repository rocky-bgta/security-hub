package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.cms.controller.UserRiskAnalysisController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.UserRiskAnalysisResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.UserRiskDetailDto;
import com.aspire.asat.cms.dto.enums.RiskCategory;
import com.aspire.asat.cms.service.UserRiskAnalysisService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller implementation for User Risk Analysis APIs
 * Provides REST endpoints for user risk-based analysis and reporting
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserRiskAnalysisControllerImpl implements UserRiskAnalysisController {

    private final UserRiskAnalysisService userRiskAnalysisService;
    private final UserCurrentContextService userCurrentContextService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<ApiResponseDto<UserRiskAnalysisResponseDto>> getUserRiskAnalysis(String productId) {
        log.info("Received request for user risk analysis, productId: {}", productId);

        try {
            // Get client admin ID from current user context
            String clientAdminId = userCurrentContextService.getCurrentUserContext().getUserId();
            log.info("Retrieved client admin ID from context: {}, productId: {}", clientAdminId, productId);

            UserRiskAnalysisResponseDto riskAnalysis = userRiskAnalysisService.generateUserRiskAnalysis(clientAdminId, productId);
            
            ApiResponseDto<UserRiskAnalysisResponseDto> response = new ApiResponseDto<>(
                    "User risk analysis generated successfully",
                    HttpStatus.OK.value(),
                    riskAnalysis
            );

            log.info("User risk analysis completed for client admin: {}, productId: {} - Total users: {}, Safe: {}, Low Risk: {}, Average Risk: {}, High Risk: {}", 
                    clientAdminId, productId, riskAnalysis.getTotalUsers(), riskAnalysis.getSafeUsers(), 
                    riskAnalysis.getLowRisk(), riskAnalysis.getAverageRisk(), riskAnalysis.getHighRisk());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating user risk analysis, productId: {}", productId, e);
            
            ApiResponseDto<UserRiskAnalysisResponseDto> errorResponse = new ApiResponseDto<>(
                    "Failed to generate user risk analysis: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<UserRiskDetailDto>>> getUserRiskAnalysisDetails(
            RiskCategory riskCategory, Integer offset, Integer pageSize) {
        
        log.info("Received request for detailed user risk analysis, risk category: {}, offset: {}, pageSize: {}", 
                riskCategory, offset, pageSize);

        try {
            // Get client admin ID from current user context
            String clientAdminId = userCurrentContextService.getCurrentUserContext().getUserId();
            log.info("Retrieved client admin ID from context: {}", clientAdminId);

            List<UserRiskDetailDto> riskDetails = userRiskAnalysisService.getUserRiskAnalysisDetails(
                    clientAdminId, riskCategory, offset, pageSize);

            ApiResponseDto<List<UserRiskDetailDto>> response = new ApiResponseDto<>(
                    "Detailed user risk analysis retrieved successfully",
                    HttpStatus.OK.value(),
                    riskDetails
            );

            log.info("Detailed user risk analysis completed for client admin: {} - Retrieved {} users", 
                    clientAdminId, riskDetails.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting detailed user risk analysis", e);
            
            ApiResponseDto<List<UserRiskDetailDto>> errorResponse = new ApiResponseDto<>(
                    "Failed to get detailed user risk analysis: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> exportUserRiskAnalysis() {
        log.info("Received request to export user risk analysis");

        try {
            // Get client admin ID from current user context
            String clientAdminId = userCurrentContextService.getCurrentUserContext().getUserId();
            log.info("Retrieved client admin ID from context: {}", clientAdminId);

            String downloadUrl = userRiskAnalysisService.exportUserRiskAnalysis(clientAdminId);
            
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    messageService.get(MessageKeys.REPORTS_EXPORTED),
                    HttpStatus.OK.value(),
                    downloadUrl
            );

            log.info("User risk analysis exported successfully for client admin: {}, download URL: {}", 
                    clientAdminId, downloadUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error exporting user risk analysis", e);
            
            ApiResponseDto<String> errorResponse = new ApiResponseDto<>(
                    "Failed to export user risk analysis: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
