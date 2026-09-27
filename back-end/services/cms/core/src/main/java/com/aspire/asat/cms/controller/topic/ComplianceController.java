package com.aspire.asat.cms.controller.topic;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.topic.ComplianceReqDto;
import com.aspire.asat.cms.dto.topic.ComplianceRespDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.COMPLIANCE_API)
@Tag(name = "Compliance API", description = "APIs for managing compliances")
public interface ComplianceController {
    @PostMapping
    @Operation(summary = "Create a new compliance", description = "Creates a new compliance with the provided details")
    @ApiResponses ({
            @ApiResponse (responseCode = "201", description = "Compliance created successfully"),
            @ApiResponse (responseCode = "400", description = "Invalid request data"),
            @ApiResponse (responseCode = "409", description = "Compliance with same name/acronym already exists")
    })
    ResponseEntity<ApiResponseDto<ComplianceRespDto>> createCompliance(@Valid @RequestBody ComplianceReqDto complianceReqDto);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update an existing compliance", description = "Updates compliance details by ID")
    @ApiResponses ({
            @ApiResponse (responseCode = "200", description = "Compliance updated successfully"),
            @ApiResponse (responseCode = "400", description = "Invalid request data"),
            @ApiResponse (responseCode = "404", description = "Compliance not found"),
            @ApiResponse (responseCode = "409", description = "Compliance with same name/acronym already exists")
    })
    ResponseEntity<ApiResponseDto<ComplianceRespDto>> updateCompliance(@PathVariable("id") String id, @RequestBody ComplianceReqDto complianceReqDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get compliance by ID", description = "Retrieves compliance details by ID")
    @ApiResponses ({
            @ApiResponse (responseCode = "200", description = "Compliance retrieved successfully"),
            @ApiResponse (responseCode = "404", description = "Compliance not found")
    })
    ResponseEntity<ApiResponseDto<ComplianceRespDto>> getComplianceById(@PathVariable("id") String id);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete compliance by ID", description = "Deletes compliance by ID")
    @ApiResponses ({
            @ApiResponse (responseCode = "200", description = "Compliance deleted successfully"),
            @ApiResponse (responseCode = "404", description = "Compliance not found")
    })
    ResponseEntity<ApiResponseDto<Void>> deleteComplianceById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "Get all compliances", description = "Retrieves list of all compliances")
    @ApiResponse (responseCode = "200", description = "Compliances retrieved successfully")
    ResponseEntity<ApiResponseDto<List<ComplianceRespDto>>> getAllCompliances();
}
