package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateReqDto;
import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateRespDto;
import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateStatusUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = WebApiUrlConstants.CLIENT_CERTIFICATE_TEMPLATE_API, produces = "application/json")
public interface ClientCertificateTemplateController {

    @Operation(summary = "Create or update client certificate template",
            description = "Creates a new client certificate template or updates existing active template for the client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template created successfully"),
            @ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> createOrUpdateTemplate(
            @Valid @RequestBody ClientCertificateTemplateReqDto reqDto);

    @Operation(summary = "Update client certificate template active status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PutMapping(value = WebApiUrlConstants.PATH_VAR_ID + "/status")
    ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> updateStatus(
            @PathVariable("id") String templateId,
            @Valid @RequestBody ClientCertificateTemplateStatusUpdateDto statusUpdateDto);

    @Operation(summary = "Get active client certificate template for current client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No active template found")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> getClientCertificateTemplate();

    @Operation(summary = "Get client certificate template by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> getById(
            @PathVariable("id") String templateId);

    @Operation(summary = "Get client certificate template by client admin user ID",
            description = "Fetches the active client certificate template for the specified client admin user ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template fetched successfully"),
            @ApiResponse(responseCode = "404", description = "No active template found for the given user ID")
    })
    @GetMapping(value = "/by-client-admin")
    ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> getByClientAdminUserId();

}

