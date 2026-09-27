package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.response.CampaignLicenseUsageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Internal Campaign License Usage", description = "Service-to-service unique campaign participant counts")
@RequestMapping(WebApiUrlConstants.INTERNAL_CAMPAIGN_LICENSE_USAGE_PATH)
public interface CampaignLicenseUsageInternalController {

    @Operation(summary = "Unique campaign users per productPackageId",
            description = "Returns unique licensed user counts from phishing_user_licence, "
                    + "grouped by productPackageId (ClientProduct.id). Requires X-Internal-Service-Key.")
    @GetMapping
    ResponseEntity<ApiResponseDto<List<CampaignLicenseUsageDto>>> getCampaignLicenseUsage(
            @Parameter(description = "Client admin id", required = true)
            @RequestParam("clientId") String clientId,
            @Parameter(description = "Optional ClientProduct.id filter")
            @RequestParam(value = "productPackageId", required = false) String productPackageId);
}
