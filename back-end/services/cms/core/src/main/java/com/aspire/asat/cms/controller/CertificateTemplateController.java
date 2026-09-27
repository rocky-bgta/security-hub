package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateReqDto;
import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateRespDto;
import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateStatusUpdateDto;
import com.aspire.asat.cms.dto.certificateTemplate.ClientAdminCertificateTemplateRespDto;
import com.aspire.asat.cms.dto.enums.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.CERTIFICATE_TEMPLATE_API, produces = "application/json")
public interface CertificateTemplateController {

    @Operation(summary = "Get all certificate templates with pagination and search")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CertificateTemplateRespDto>>>> getAllTemplates(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order);

    @Operation(summary = "Get all certificate templates without pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    ResponseEntity<ApiResponseDto<List<CertificateTemplateRespDto>>> getAllTemplatesList();

    @Operation(summary = "Get certificate template by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> getTemplateById(@PathVariable("id") String templateId);

    @Operation(summary = "Create a new certificate template")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> createTemplate(
            @Valid @RequestBody CertificateTemplateReqDto reqDto);

    @Operation(summary = "Update an existing certificate template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> updateTemplate(
            @PathVariable("id") String templateId,
            @Valid @RequestBody CertificateTemplateReqDto reqDto);

    @Operation(summary = "Update certificate template status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PutMapping(value = WebApiUrlConstants.PATH_VAR_ID + "/status")
    ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> updateTemplateStatus(
            @PathVariable("id") String templateId,
            @Valid @RequestBody CertificateTemplateStatusUpdateDto statusUpdateDto);

    @Operation(summary = "Delete a certificate template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> deleteTemplate(@PathVariable("id") String templateId);

    /**
     * Get the default certificate template.
     *
     * @return The default certificate template response DTO
     */
    @Operation(summary = "Get the default certificate template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Default template fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Default template not found")
    })
    @GetMapping("/default")
    ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> getDefaultTemplate();

    /**
     * Get certificate templates for client admin with assignment status.
     * Always includes default and assigned templates exclusively.
     * Other templates are returned with pagination.
     *
     * @param search   Search term for template name (optional)
     * @param offset   Number of records to skip
     * @param pageSize Number of records to return
     * @param sortBy   Field to sort by
     * @param order    Sort order (asc/desc)
     * @return Paginated list of certificate templates with isAssigned flag
     */
    @Operation(summary = "Get certificate templates for client admin with assignment status and pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client-admin-template")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ClientAdminCertificateTemplateRespDto>>>> getClientAdminTemplates(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order);

    @Operation(summary = "Check if certificate template is configured for a client", 
               description = "Returns true if a certificate template exists in client_certificate_template table for the given clientId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid clientId parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/check-exists")
    ResponseEntity<ApiResponseDto<Boolean>> checkCertificateTemplateExists(
            @RequestParam(value = "clientId", required = true) String clientId);
}
