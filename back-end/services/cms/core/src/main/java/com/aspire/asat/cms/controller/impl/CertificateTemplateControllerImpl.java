package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.CertificateTemplateController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateReqDto;
import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateRespDto;
import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateStatusUpdateDto;
import com.aspire.asat.cms.dto.certificateTemplate.ClientAdminCertificateTemplateRespDto;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.service.CertificateTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CertificateTemplateControllerImpl implements CertificateTemplateController {

    private final CertificateTemplateService certificateTemplateService;

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<CertificateTemplateRespDto>>>> getAllTemplates(
            String search, Status status, Integer offset, Integer pageSize, String sortBy, String order) {
        log.info("Fetching certificate templates with search: {}, status: {}, offset: {}, pageSize: {}",
                search, status, offset, pageSize);

        List<CertificateTemplateRespDto> templates = certificateTemplateService.getTemplatesWithPagination(
                search, status, offset, pageSize, sortBy, order);
        long totalCount = certificateTemplateService.getTotalTemplateCount(search, status);

        AllResponseDto<List<CertificateTemplateRespDto>> allResponseDto =
                new AllResponseDto<>(offset, pageSize, totalCount, templates);
        ApiResponseDto<AllResponseDto<List<CertificateTemplateRespDto>>> response =
                new ApiResponseDto<>("Certificate templates fetched successfully", 200, allResponseDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<CertificateTemplateRespDto>>> getAllTemplatesList() {
        log.info("Fetching all certificate templates without pagination");

        List<CertificateTemplateRespDto> templates = certificateTemplateService.getAllTemplates();
        ApiResponseDto<List<CertificateTemplateRespDto>> response =
                new ApiResponseDto<>("Certificate templates fetched successfully", 200, templates);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> getTemplateById(String templateId) {
        log.info("Fetching certificate template by ID: {}", templateId);

        CertificateTemplateRespDto template = certificateTemplateService.getTemplateById(templateId);
        ApiResponseDto<CertificateTemplateRespDto> response =
                new ApiResponseDto<>("Certificate template fetched successfully", 200, template);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> createTemplate(CertificateTemplateReqDto reqDto) {
        log.info("Creating new certificate template with name: {}", reqDto.getTemplateName());

        CertificateTemplateRespDto createdTemplate = certificateTemplateService.createTemplate(reqDto);
        ApiResponseDto<CertificateTemplateRespDto> response =
                new ApiResponseDto<>("Certificate template created successfully", 201, createdTemplate);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> updateTemplate(
            String templateId, CertificateTemplateReqDto reqDto) {
        log.info("Updating certificate template with ID: {}", templateId);

        CertificateTemplateRespDto updatedTemplate = certificateTemplateService.updateTemplate(templateId, reqDto);
        ApiResponseDto<CertificateTemplateRespDto> response =
                new ApiResponseDto<>("Certificate template updated successfully", 200, updatedTemplate);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> updateTemplateStatus(
            String templateId, CertificateTemplateStatusUpdateDto statusUpdateDto) {
        log.info("Updating certificate template status. ID: {}, Status: {}", templateId, statusUpdateDto.getStatus());

        CertificateTemplateRespDto updatedTemplate =
                certificateTemplateService.updateTemplateStatus(templateId, statusUpdateDto.getStatus());
        ApiResponseDto<CertificateTemplateRespDto> response =
                new ApiResponseDto<>("Certificate template status updated successfully", 200, updatedTemplate);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> deleteTemplate(String templateId) {
        log.info("Deleting certificate template with ID: {}", templateId);

        CertificateTemplateRespDto deletedTemplate = certificateTemplateService.deleteTemplate(templateId);
        ApiResponseDto<CertificateTemplateRespDto> response =
                new ApiResponseDto<>("Certificate template deleted successfully", 200, deletedTemplate);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CertificateTemplateRespDto>> getDefaultTemplate() {
        log.info("Fetching the default certificate template");

        CertificateTemplateRespDto defaultTemplate = certificateTemplateService.getDefaultTemplate();
        ApiResponseDto<CertificateTemplateRespDto> response =
                new ApiResponseDto<>("Default certificate template fetched successfully", 200, defaultTemplate);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ClientAdminCertificateTemplateRespDto>>>> getClientAdminTemplates(
            String search, Integer offset, Integer pageSize, String sortBy, String order) {
        log.info("Fetching certificate templates for client admin with assignment status - search: {}, offset: {}, pageSize: {}",
                search, offset, pageSize);

        List<ClientAdminCertificateTemplateRespDto> templates = certificateTemplateService.getClientAdminTemplates(
                search, offset, pageSize, sortBy, order);
        long totalCount = certificateTemplateService.getClientAdminTemplatesCount(search);

        AllResponseDto<List<ClientAdminCertificateTemplateRespDto>> allResponseDto =
                new AllResponseDto<>(offset, pageSize, totalCount, templates);
        ApiResponseDto<AllResponseDto<List<ClientAdminCertificateTemplateRespDto>>> response =
                new ApiResponseDto<>("Certificate templates fetched successfully", 200, allResponseDto);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> checkCertificateTemplateExists(String clientId) {
        log.info("Checking certificate template existence for clientId: {}", clientId);
        
        if (clientId == null || clientId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("ClientId parameter is required", 400, false));
        }
        
        boolean exists = certificateTemplateService.checkCertificateTemplateExists(clientId.trim());
        ApiResponseDto<Boolean> response = new ApiResponseDto<>(
                "Certificate template check completed successfully", 200, exists);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

