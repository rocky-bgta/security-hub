package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateReqDto;
import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateRespDto;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.ClientCertificateTemplate;
import com.aspire.asat.cms.repository.ClientCertificateTemplateRepository;
import com.aspire.asat.cms.service.ClientCertificateTemplateService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientCertificateTemplateServiceImpl implements ClientCertificateTemplateService {

    private final ClientCertificateTemplateRepository clientCertificateTemplateRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ClientCertificateTemplateRespDto createOrUpdateTemplate(ClientCertificateTemplateReqDto reqDto) {
        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();
        String clientAdminId = currentUser.getUserId();
        Instant now = Instant.now();

        log.info("Creating or updating client certificate template for clientAdminId: {}", clientAdminId);

        // Check if an active template already exists for this client
        Optional<ClientCertificateTemplate> existingTemplateOpt =
                clientCertificateTemplateRepository.findByClientAdminIdAndActive(clientAdminId, true);

        ClientCertificateTemplate template;

        if (existingTemplateOpt.isPresent()) {
            // Update existing active template
            template = existingTemplateOpt.get();
            log.info("Updating existing active template with ID: {}", template.getId());

            template.setTemplateId(reqDto.getTemplateId());
            template.setUpdatedBy(currentUser.getUserId());
            template.setUpdatedAt(now);
        } else {
            // Create new template
            log.info("Creating new client certificate template for clientAdminId: {}", clientAdminId);

            template = ClientCertificateTemplate.builder()
                    .templateId(reqDto.getTemplateId())
                    .clientAdminId(clientAdminId)
                    .active(true)
                    .createdBy(currentUser.getUserId())
                    .createdAt(now)
                    .updatedBy(currentUser.getUserId())
                    .updatedAt(now)
                    .build();
        }

        ClientCertificateTemplate savedTemplate = clientCertificateTemplateRepository.save(template);
        log.info("Client certificate template saved successfully with ID: {}", savedTemplate.getId());

        return mapToResponseDto(savedTemplate);
    }

    @Override
    public ClientCertificateTemplateRespDto updateStatus(String templateId, boolean active) {
        log.info("Updating client certificate template status. ID: {}, Active: {}", templateId, active);

        ClientCertificateTemplate template = clientCertificateTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client certificate template not found with ID: " + templateId));

        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();

        template.setActive(active);
        template.setUpdatedBy(currentUser.getUserId());
        template.setUpdatedAt(Instant.now());

        ClientCertificateTemplate updatedTemplate = clientCertificateTemplateRepository.save(template);
        log.info("Client certificate template status updated successfully. ID: {}, Active: {}",
                templateId, active);

        return mapToResponseDto(updatedTemplate);
    }

    @Override
    public ClientCertificateTemplateRespDto getClientCertificateTemplate() {
        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();

        // Determine clientAdminId based on user type
        String clientAdminId;
        if ("CLIENT_ADMIN".equals(currentUser.getUserType())) {
            clientAdminId = currentUser.getUserId();
        } else {
            // For USER type, use clientAdminId
            clientAdminId = currentUser.getClientAdminId();
        }

        log.info("Fetching client certificate template for clientAdminId: {}, userType: {}",
                clientAdminId, currentUser.getUserType());

        ClientCertificateTemplate template = clientCertificateTemplateRepository
                .findByClientAdminIdAndActive(clientAdminId, true)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active client certificate template found for clientAdminId: " + clientAdminId));

        return mapToResponseDto(template);
    }

    @Override
    public ClientCertificateTemplateRespDto getById(String templateId) {
        log.info("Fetching client certificate template by ID: {}", templateId);

        ClientCertificateTemplate template = clientCertificateTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client certificate template not found with ID: " + templateId));

        return mapToResponseDto(template);
    }

    @Override
    public ClientCertificateTemplateRespDto getByClientAdminUserId() {
        log.info("Fetching client certificate template by client admin user ID");

        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();

        // Determine clientAdminId based on user type
        String clientAdminId;
        if ("CLIENT_ADMIN".equals(currentUser.getUserType())) {
            clientAdminId = currentUser.getUserId();
        } else {
            // For USER type, use clientAdminId
            clientAdminId = currentUser.getClientAdminId();
        }

        ClientCertificateTemplate template = clientCertificateTemplateRepository
                .findByClientAdminIdAndActive(clientAdminId, true)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active client certificate template found for client admin user ID: " + clientAdminId));

        return mapToResponseDto(template);
    }

    private ClientCertificateTemplateRespDto mapToResponseDto(ClientCertificateTemplate template) {
        return ClientCertificateTemplateRespDto.builder()
                .id(template.getId())
                .templateId(template.getTemplateId())
                .clientAdminId(template.getClientAdminId())
                .active(template.isActive())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedBy(template.getUpdatedBy())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}

