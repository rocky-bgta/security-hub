package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.cms.controller.ClientCertificateTemplateController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateReqDto;
import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateRespDto;
import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateStatusUpdateDto;
import com.aspire.asat.cms.service.ClientCertificateTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClientCertificateTemplateControllerImpl implements ClientCertificateTemplateController {

    private final ClientCertificateTemplateService clientCertificateTemplateService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> createOrUpdateTemplate(
            ClientCertificateTemplateReqDto reqDto) {
        log.info("Creating or updating client certificate template with templateId: {}", reqDto.getTemplateId());

        ClientCertificateTemplateRespDto result = clientCertificateTemplateService.createOrUpdateTemplate(reqDto);
        ApiResponseDto<ClientCertificateTemplateRespDto> response =
                new ApiResponseDto<>(messageService.get(MessageKeys.TEMPLATE_ASSIGNED_SUCCESS), 200, result);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> updateStatus(
            String templateId, ClientCertificateTemplateStatusUpdateDto statusUpdateDto) {
        log.info("Updating client certificate template status. ID: {}, Active: {}",
                templateId, statusUpdateDto.getActive());

        ClientCertificateTemplateRespDto result =
                clientCertificateTemplateService.updateStatus(templateId, statusUpdateDto.getActive());
        ApiResponseDto<ClientCertificateTemplateRespDto> response =
                new ApiResponseDto<>("Client certificate template status updated successfully", 200, result);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> getClientCertificateTemplate() {
        log.info("Fetching active client certificate template for current client");

        ClientCertificateTemplateRespDto result = clientCertificateTemplateService.getClientCertificateTemplate();
        ApiResponseDto<ClientCertificateTemplateRespDto> response =
                new ApiResponseDto<>("Client certificate template fetched successfully", 200, result);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> getById(String templateId) {
        log.info("Fetching client certificate template by ID: {}", templateId);

        ClientCertificateTemplateRespDto result = clientCertificateTemplateService.getById(templateId);
        ApiResponseDto<ClientCertificateTemplateRespDto> response =
                new ApiResponseDto<>("Client certificate template fetched successfully", 200, result);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientCertificateTemplateRespDto>> getByClientAdminUserId() {
        log.info("Fetching client certificate template by client admin user ID");

        ClientCertificateTemplateRespDto result = clientCertificateTemplateService.getByClientAdminUserId();
        ApiResponseDto<ClientCertificateTemplateRespDto> response =
                new ApiResponseDto<>("Client certificate template fetched successfully", 200, result);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

