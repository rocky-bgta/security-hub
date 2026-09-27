package com.aspire.asat.cms.controller.reports;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.reports.TrainingCertificateCountResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Training Certificate Counts",
        description = "Internal counts for training completion and certificates earned by client admin")
@RequestMapping(value = WebApiUrlConstants.TRAINING_CERTIFICATE_COUNT_API, produces = "application/json")
public interface TrainingCertificateCountController {

    @GetMapping
    @Operation(
            summary = "Get training completed and certificate earned counts",
            description = "Returns count of COMPLETED user_subpackages and user_certificates for the given clientAdminId."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Counts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid clientAdminId"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<TrainingCertificateCountResponseDto>> getTrainingCertificateCounts(
            @Parameter(description = "Client admin ID", required = true)
            @RequestParam String clientAdminId
    );
}
