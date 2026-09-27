package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateReqDto;
import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateRespDto;
import com.aspire.asat.cms.dto.certificateTemplate.ClientAdminCertificateTemplateRespDto;
import com.aspire.asat.cms.dto.certificateTemplate.DynamicFieldsDto;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.CertificateTemplate;
import com.aspire.asat.cms.model.ClientCertificateTemplate;
import com.aspire.asat.cms.model.DynamicFields;
import com.aspire.asat.cms.repository.CertificateTemplateRepository;
import com.aspire.asat.cms.repository.ClientCertificateTemplateRepository;
import com.aspire.asat.cms.service.CertificateTemplateService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateTemplateServiceImpl implements CertificateTemplateService {

    private final CertificateTemplateRepository certificateTemplateRepository;
    private final ClientCertificateTemplateRepository clientCertificateTemplateRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final WebClient webClient;

    @Value("${service.registration.url}")
    private String registrationUrl;

    @Override
    public CertificateTemplateRespDto createTemplate(CertificateTemplateReqDto reqDto) {
        log.info("Creating certificate template with name: {}", reqDto.getTemplateName());

        // Check if template name already exists
        if (certificateTemplateRepository.existsByTemplateName(reqDto.getTemplateName())) {
            throw new DuplicateNameException("Certificate template with name '" + reqDto.getTemplateName() + "' already exists");
        }

        if (Boolean.TRUE.equals(reqDto.getIsDefault())) {
            certificateTemplateRepository.findByIsDefaultTrue()
                .ifPresent(existingTemplate -> {
                    existingTemplate.setIsDefault(false);
                    certificateTemplateRepository.save(existingTemplate);
                });
        }

        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();
        Instant now = Instant.now();

        CertificateTemplate template = CertificateTemplate.builder()
                .templateName(reqDto.getTemplateName())
                .certificateTitle(reqDto.getCertificateTitle())
                .certificateType(reqDto.getCertificateType())
                .acknowledgement(reqDto.getAcknowledgement())
                .completionStatus(reqDto.getCompletionStatus())
                .completionTitle(reqDto.getCompletionTitle())
                .logoImageUrl(reqDto.getLogoImageUrl())
                .signatureImageUrl(reqDto.getSignatureImageUrl())
                .signerName(reqDto.getSignerName())
                .signerDesignation(reqDto.getSignerDesignation())
                .signatureIdentity(reqDto.getSignatureIdentity())
                .backgroundImageUrl(reqDto.getBackgroundImageUrl())
                .dynamicFields(mapDynamicFields(reqDto.getDynamicFields()))
                .status(reqDto.getStatus() != null ? reqDto.getStatus() : Status.ENABLED)
                .isDefault(reqDto.getIsDefault() != null ? reqDto.getIsDefault() : false)
                .createdBy(currentUser.getUserId())
                .createdAt(now)
                .updatedBy(currentUser.getUserId())
                .updatedAt(now)
                .isTrialTemplate(reqDto.getIsTrialTemplate() != null ? reqDto.getIsTrialTemplate() : false)
                .build();

        CertificateTemplate savedTemplate = certificateTemplateRepository.save(template);
        log.info("Certificate template created successfully with ID: {}", savedTemplate.getId());

        return mapToResponseDto(savedTemplate);
    }

    @Override
    public CertificateTemplateRespDto updateTemplate(String templateId, CertificateTemplateReqDto reqDto) {
        log.info("Updating certificate template with ID: {}", templateId);

        CertificateTemplate existingTemplate = certificateTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate template not found with ID: " + templateId));

        // Check if template name already exists (excluding current template)
        if (!existingTemplate.getTemplateName().equals(reqDto.getTemplateName())
                && certificateTemplateRepository.existsByTemplateName(reqDto.getTemplateName())) {
            throw new DuplicateNameException("Certificate template with name '" + reqDto.getTemplateName() + "' already exists");
        }

        if (Boolean.TRUE.equals(reqDto.getIsDefault())) {
            certificateTemplateRepository.findByIsDefaultTrue()
                .ifPresent(existingDefaultTemplate -> {
                    if (!existingDefaultTemplate.getId().equals(templateId)) {
                        existingDefaultTemplate.setIsDefault(false);
                        certificateTemplateRepository.save(existingDefaultTemplate);
                    }
                });
        }

        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();

        existingTemplate.setTemplateName(reqDto.getTemplateName());
        existingTemplate.setCertificateTitle(reqDto.getCertificateTitle());
        existingTemplate.setCertificateType(reqDto.getCertificateType());
        existingTemplate.setAcknowledgement(reqDto.getAcknowledgement());
        existingTemplate.setCompletionStatus(reqDto.getCompletionStatus());
        existingTemplate.setCompletionTitle(reqDto.getCompletionTitle());
        existingTemplate.setLogoImageUrl(reqDto.getLogoImageUrl());
        existingTemplate.setSignatureImageUrl(reqDto.getSignatureImageUrl());
        existingTemplate.setSignerName(reqDto.getSignerName());
        existingTemplate.setSignerDesignation(reqDto.getSignerDesignation());
        existingTemplate.setSignatureIdentity(reqDto.getSignatureIdentity());
        existingTemplate.setBackgroundImageUrl(reqDto.getBackgroundImageUrl());
        existingTemplate.setDynamicFields(mapDynamicFields(reqDto.getDynamicFields()));
        if (reqDto.getStatus() != null) {
            existingTemplate.setStatus(reqDto.getStatus());
        }
        if (reqDto.getIsDefault() != null) {
            existingTemplate.setIsDefault(reqDto.getIsDefault());
        }
        existingTemplate.setIsTrialTemplate(reqDto.getIsTrialTemplate() != null ? reqDto.getIsTrialTemplate() : false);
        existingTemplate.setUpdatedBy(currentUser.getUserId());
        existingTemplate.setUpdatedAt(Instant.now());

        CertificateTemplate updatedTemplate = certificateTemplateRepository.save(existingTemplate);
        log.info("Certificate template updated successfully with ID: {}", updatedTemplate.getId());

        return mapToResponseDto(updatedTemplate);
    }

    @Override
    public CertificateTemplateRespDto getTemplateById(String templateId) {
        log.info("Fetching certificate template by ID: {}", templateId);

        CertificateTemplate template = certificateTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate template not found with ID: " + templateId));

        return mapToResponseDto(template);
    }

    @Override
    public List<CertificateTemplateRespDto> getAllTemplates() {
        log.info("Fetching all certificate templates");

        return certificateTemplateRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CertificateTemplateRespDto> getTemplatesWithPagination(
            String search, Status status, Integer offset, Integer pageSize, String sortBy, String order) {
        log.info("Fetching certificate templates with pagination - search: {}, status: {}, offset: {}, pageSize: {}",
                search, status, offset, pageSize);

        int effectiveOffset = offset != null ? offset : 0;
        int effectivePageSize = pageSize != null ? pageSize : 10;
        String effectiveSortBy = sortBy != null ? sortBy : "createdAt";
        String effectiveOrder = order != null ? order : "desc";

        return certificateTemplateRepository.findAllWithPaginationAndSearch(
                        search, status, effectiveOffset, effectivePageSize, effectiveSortBy, effectiveOrder)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalTemplateCount(String search, Status status) {
        log.info("Counting certificate templates - search: {}, status: {}", search, status);
        return certificateTemplateRepository.countWithSearch(search, status);
    }

    @Override
    public CertificateTemplateRespDto updateTemplateStatus(String templateId, Status status) {
        log.info("Updating certificate template status. ID: {}, Status: {}", templateId, status);

        CertificateTemplate existingTemplate = certificateTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate template not found with ID: " + templateId));

        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();

        existingTemplate.setStatus(status);
        existingTemplate.setUpdatedBy(currentUser.getUserId());
        existingTemplate.setUpdatedAt(Instant.now());

        CertificateTemplate updatedTemplate = certificateTemplateRepository.save(existingTemplate);
        log.info("Certificate template status updated successfully. ID: {}, New Status: {}", templateId, status);

        return mapToResponseDto(updatedTemplate);
    }

    @Override
    public CertificateTemplateRespDto deleteTemplate(String templateId) {
        log.info("Deleting certificate template with ID: {}", templateId);

        CertificateTemplate template = certificateTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate template not found with ID: " + templateId));

        certificateTemplateRepository.delete(template);
        log.info("Certificate template deleted successfully with ID: {}", templateId);

        return mapToResponseDto(template);
    }

    @Override
    public CertificateTemplateRespDto getDefaultTemplate() {
        log.info("Fetching the default certificate template");

        CertificateTemplate defaultTemplate = certificateTemplateRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No default certificate template found"));

        return mapToResponseDto(defaultTemplate);
    }

    private DynamicFields mapDynamicFields(DynamicFieldsDto dto) {
        if (dto == null) {
            return DynamicFields.builder()
                    .showLearnerName(true)
                    .showCourseName(true)
                    .showIssueDate(true)
                    .showCertificateId(true)
                    .showQrCode(false)
                    .build();
        }

        return DynamicFields.builder()
                .showLearnerName(dto.isShowLearnerName())
                .showCourseName(dto.isShowCourseName())
                .showIssueDate(dto.isShowIssueDate())
                .showCertificateId(dto.isShowCertificateId())
                .showQrCode(dto.isShowQrCode())
                .build();
    }

    private CertificateTemplateRespDto mapToResponseDto(CertificateTemplate template) {
        DynamicFieldsDto dynamicFieldsDto = null;

        if (template.getDynamicFields() != null) {
            dynamicFieldsDto = DynamicFieldsDto.builder()
                    .showLearnerName(template.getDynamicFields().isShowLearnerName())
                    .showCourseName(template.getDynamicFields().isShowCourseName())
                    .showIssueDate(template.getDynamicFields().isShowIssueDate())
                    .showCertificateId(template.getDynamicFields().isShowCertificateId())
                    .showQrCode(template.getDynamicFields().isShowQrCode())
                    .build();
        }

        return CertificateTemplateRespDto.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .certificateTitle(template.getCertificateTitle())
                .certificateType(template.getCertificateType())
                .acknowledgement(template.getAcknowledgement())
                .completionStatus(template.getCompletionStatus())
                .completionTitle(template.getCompletionTitle())
                .logoImageUrl(template.getLogoImageUrl())
                .signatureImageUrl(template.getSignatureImageUrl())
                .signerName(template.getSignerName())
                .signerDesignation(template.getSignerDesignation())
                .signatureIdentity(template.getSignatureIdentity())
                .backgroundImageUrl(template.getBackgroundImageUrl())
                .dynamicFields(dynamicFieldsDto)
                .status(template.getStatus())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedBy(template.getUpdatedBy())
                .updatedAt(template.getUpdatedAt())
                .isDefault(template.getIsDefault())
                .isTrialTemplate(template.getIsTrialTemplate())
                .build();
    }

    @Override
    public List<ClientAdminCertificateTemplateRespDto> getClientAdminTemplates(
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    ) {
        log.info("Fetching certificate templates for client admin with assignment status - search: {}, offset: {}, pageSize: {}",
                search, offset, pageSize);

        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();
        String clientAdminId = currentUser.getUserId();
        String onboardBy = currentUser.getOnboardBy();

        List<ClientAdminCertificateTemplateRespDto> result = new java.util.ArrayList<>();
        List<String> excludeIds = new java.util.ArrayList<>();

        // Handle TRIAL vs non-TRIAL users differently
        if ("TRIAL".equalsIgnoreCase(onboardBy)) {
            processTrialUserTemplates(result, excludeIds);
        } else {
            processNonTrialUserTemplates(clientAdminId, result, excludeIds);
        }

        // Fetch and add other templates with pagination
        addOtherTemplates(result, excludeIds, search, offset, pageSize, sortBy, order);

        return result;
    }

    /**
     * Process templates for TRIAL users:
     * - Add trial template with isAssigned = true
     * - Add default template with isAssigned = false
     */
    private void processTrialUserTemplates(
            List<ClientAdminCertificateTemplateRespDto> result,
            List<String> excludeIds
    ) {
        log.info("Processing templates for TRIAL user");

        // Get and add trial template
        certificateTemplateRepository.findByIsTrialTemplateTrue()
                .ifPresent(trialTemplate -> {
                    excludeIds.add(trialTemplate.getId());
                    result.add(mapToClientAdminResponseDto(trialTemplate, true));
                });

        // Get and add default template (not as assigned)
        certificateTemplateRepository.findByIsDefaultTrue()
                .ifPresent(defaultTemplate -> {
                    if (!excludeIds.contains(defaultTemplate.getId())) {
                        excludeIds.add(defaultTemplate.getId());
                    }
                    result.add(mapToClientAdminResponseDto(defaultTemplate, false));
                });
    }

    /**
     * Process templates for non-TRIAL users:
     * - If user has assigned template, add it with isAssigned = true
     * - Otherwise, add default template with isAssigned = true
     */
    private void processNonTrialUserTemplates(
            String clientAdminId,
            List<ClientAdminCertificateTemplateRespDto> result,
            List<String> excludeIds
    ) {
        log.info("Processing templates for non-TRIAL user: {}", clientAdminId);

        Optional<CertificateTemplate> defaultTemplateOpt = certificateTemplateRepository.findByIsDefaultTrue();
        Optional<ClientCertificateTemplate> clientTemplateOpt =
                clientCertificateTemplateRepository.findByClientAdminIdAndActive(clientAdminId, true);

        if (clientTemplateOpt.isPresent()) {
            // User has an assigned template
            String assignedTemplateId = clientTemplateOpt.get().getTemplateId();
            certificateTemplateRepository.findById(assignedTemplateId)
                    .ifPresent(assignedTemplate -> {
                        excludeIds.add(assignedTemplateId);
                        result.add(mapToClientAdminResponseDto(assignedTemplate, true));
                    });
        } else if (defaultTemplateOpt.isPresent()) {
            // No assigned template, use default as assigned
            CertificateTemplate defaultTemplate = defaultTemplateOpt.get();
            excludeIds.add(defaultTemplate.getId());
            result.add(mapToClientAdminResponseDto(defaultTemplate, true));
        }
    }

    /**
     * Fetch and add other templates (excluding already added ones) with pagination
     */
    private void addOtherTemplates(
            List<ClientAdminCertificateTemplateRespDto> result,
            List<String> excludeIds,
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    ) {
        int effectiveOffset = offset != null ? offset : 0;
        int effectivePageSize = pageSize != null ? pageSize : 10;
        String effectiveSortBy = sortBy != null ? sortBy : "createdAt";
        String effectiveOrder = order != null ? order : "desc";

        List<CertificateTemplate> otherTemplates = certificateTemplateRepository
                .findAllExcludingIdsWithPaginationAndSearch(
                        search, Status.ENABLED, excludeIds, effectiveOffset, effectivePageSize,
                        effectiveSortBy, effectiveOrder);

        result.addAll(otherTemplates.stream()
                .map(template -> mapToClientAdminResponseDto(template, false))
                .collect(Collectors.toList()));
    }

    @Override
    public long getClientAdminTemplatesCount(String search) {
        log.info("Counting certificate templates for client admin - search: {}", search);

        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();
        String clientAdminId = currentUser.getUserId();

        String onboardBy = currentUser.getOnboardBy();
        long templateCount = 0;

        // Build list of IDs to exclude (default and assigned)
        List<String> excludeIds = new java.util.ArrayList<>();
        // If onboardBy is TRIAL, only fetch and return trial templates
        if ("TRIAL".equalsIgnoreCase(onboardBy)) {
            log.info("Client admin {} has onboardBy TRIAL, fetching only trial templates", clientAdminId);
            Optional<CertificateTemplate> trialTemplateOpt = certificateTemplateRepository.findByIsTrialTemplateTrue();
            
            if (trialTemplateOpt.isPresent()) {
                excludeIds.add(trialTemplateOpt.get().getId());
            } else {
                log.warn("No trial template found for client admin with onboardBy TRIAL: {}", clientAdminId);
            }
        }

        // Fetch the default template
        Optional<CertificateTemplate> defaultTemplateOpt =
                certificateTemplateRepository.findByIsDefaultTrue();

        // Fetch the client's assigned certificate template
        Optional<ClientCertificateTemplate> clientTemplateOpt =
                clientCertificateTemplateRepository.findByClientAdminIdAndActive(clientAdminId, true);

        // Determine which template should be marked as assigned
        String assignedTemplateId = null;
        if (clientTemplateOpt.isPresent()) {
            assignedTemplateId = clientTemplateOpt.get().getTemplateId();
        } else if (defaultTemplateOpt.isPresent()) {
            assignedTemplateId = defaultTemplateOpt.get().getId();
        }

        if (defaultTemplateOpt.isPresent()) {
            excludeIds.add(defaultTemplateOpt.get().getId());
        }
        if (assignedTemplateId != null && !assignedTemplateId.equals(
                defaultTemplateOpt.map(CertificateTemplate::getId).orElse(null))) {
            excludeIds.add(assignedTemplateId);
        }
        templateCount += excludeIds.size();
        long count = certificateTemplateRepository.countExcludingIdsWithSearch(search, Status.ENABLED, excludeIds);
        templateCount += count;
        return  templateCount;
    }

    private ClientAdminCertificateTemplateRespDto mapToClientAdminResponseDto(
            CertificateTemplate template, String assignedTemplateId) {
        // Determine if this template is assigned
        boolean isAssigned;
        if (assignedTemplateId != null) {
            // Client has an assigned template, check if this is the one
            isAssigned = template.getId().equals(assignedTemplateId);
        } else {
            // No assigned template, use default
            isAssigned = Boolean.TRUE.equals(template.getIsDefault());
        }

        return mapToClientAdminResponseDto(template, isAssigned);
    }

    /**
     * Fetch ClientAdmin's onboardBy value from registration service.
     * Makes a REST call to /registration/api/v1/client/admin/{clientAdminId}
     *
     * @param clientAdminId The client admin ID
     * @return onboardBy value as String (TRIAL, BUY_NOW, MSP, ASPIRE_ADMIN) or null if not found/error
     */
    private String fetchClientAdminOnboardBy(String clientAdminId) {
        try {
            String url = registrationUrl + "/client/admin/" + clientAdminId;
            log.debug("Fetching ClientAdmin onboardBy from registration service: {}", url);

            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null) {
                log.warn("No data field in registration service response for clientAdminId: {}", clientAdminId);
                return null;
            }

            JsonNode dataNode = response.get("data");
            JsonNode onboardByNode = dataNode.get("onboardBy");
            
            if (onboardByNode != null && !onboardByNode.isNull()) {
                String onboardBy = onboardByNode.asText();
                log.debug("Fetched onboardBy value: {} for clientAdminId: {}", onboardBy, clientAdminId);
                return onboardBy;
            } else {
                log.debug("onboardBy field not found in response for clientAdminId: {}", clientAdminId);
                return null;
            }

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Client admin not found in registration service: {}", clientAdminId);
            return null;
        } catch (WebClientResponseException e) {
            log.error("Error calling registration service for clientAdminId: {}. Status: {}, Response: {}", 
                    clientAdminId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching ClientAdmin onboardBy for clientAdminId: {}", clientAdminId, e);
            return null;
        }
    }

    private ClientAdminCertificateTemplateRespDto mapToClientAdminResponseDto(
            CertificateTemplate template,  boolean isAssigned) {
        DynamicFieldsDto dynamicFieldsDto = null;

        if (template.getDynamicFields() != null) {
            dynamicFieldsDto = DynamicFieldsDto.builder()
                    .showLearnerName(template.getDynamicFields().isShowLearnerName())
                    .showCourseName(template.getDynamicFields().isShowCourseName())
                    .showIssueDate(template.getDynamicFields().isShowIssueDate())
                    .showCertificateId(template.getDynamicFields().isShowCertificateId())
                    .showQrCode(template.getDynamicFields().isShowQrCode())
                    .build();
        }

        return ClientAdminCertificateTemplateRespDto.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .certificateTitle(template.getCertificateTitle())
                .certificateType(template.getCertificateType())
                .acknowledgement(template.getAcknowledgement())
                .completionStatus(template.getCompletionStatus())
                .completionTitle(template.getCompletionTitle())
                .logoImageUrl(template.getLogoImageUrl())
                .signatureImageUrl(template.getSignatureImageUrl())
                .signerName(template.getSignerName())
                .signerDesignation(template.getSignerDesignation())
                .signatureIdentity(template.getSignatureIdentity())
                .backgroundImageUrl(template.getBackgroundImageUrl())
                .dynamicFields(dynamicFieldsDto)
                .status(template.getStatus())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedBy(template.getUpdatedBy())
                .updatedAt(template.getUpdatedAt())
                .isDefault(template.getIsDefault())
                .isAssigned(isAssigned)
                .isTrialTemplate(template.getIsTrialTemplate())
                .build();
    }

    @Override
    public boolean checkCertificateTemplateExists(String clientId) {
        log.info("Checking if certificate template exists for clientId: {}", clientId);
        
        if (clientId == null || clientId.trim().isEmpty()) {
            log.warn("ClientId is null or empty, returning false");
            return false;
        }
        
        boolean exists = clientCertificateTemplateRepository.findByClientAdminId(clientId).isPresent();
        log.info("Certificate template exists for clientId {}: {}", clientId, exists);
        
        return exists;
    }
}

