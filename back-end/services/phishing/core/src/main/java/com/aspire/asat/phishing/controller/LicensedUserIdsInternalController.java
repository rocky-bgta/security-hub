package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Internal Licensed User IDs", description = "Service-to-service licensed userId lookup")
@RequestMapping(value = WebApiUrlConstants.INTERNAL_LICENSED_USER_IDS_PATH)
public interface LicensedUserIdsInternalController {

    @Operation(summary = "Licensed user IDs for a product package",
            description = "Returns distinct userIds from phishing_user_licence for client + productPackageId. "
                    + "Requires X-Internal-Service-Key.")
    @GetMapping
    ResponseEntity<ApiResponseDto<List<String>>> getLicensedUserIds(
            @Parameter(description = "Client admin id", required = true)
            @RequestParam("clientId") String clientId,
            @Parameter(description = "ClientProduct.id", required = true)
            @RequestParam("productPackageId") String productPackageId);
}
