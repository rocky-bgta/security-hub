package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.CampaignLicenseUsageInternalController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.response.CampaignLicenseUsageDto;
import com.aspire.asat.phishing.service.CampaignLicenseUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CampaignLicenseUsageInternalControllerImpl implements CampaignLicenseUsageInternalController {

    private final CampaignLicenseUsageService campaignLicenseUsageService;

    @Override
    public ResponseEntity<ApiResponseDto<List<CampaignLicenseUsageDto>>> getCampaignLicenseUsage(
            String clientId, String productPackageId) {
        try {
            List<CampaignLicenseUsageDto> usage =
                    campaignLicenseUsageService.getCampaignLicenseUsage(clientId, productPackageId);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Campaign license usage retrieved successfully", 200, usage));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid campaign license usage request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to retrieve campaign license usage for clientId={}, productPackageId={}",
                    clientId, productPackageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve campaign license usage", 500, null));
        }
    }
}
