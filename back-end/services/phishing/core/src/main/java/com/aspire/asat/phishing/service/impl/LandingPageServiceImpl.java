package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.dto.request.LandingPageCreateRequest;
import com.aspire.asat.phishing.dto.request.LandingPageUpdateRequest;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.LandingPageCategoryDto;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.dto.response.LandingPagePreviewDto;
import com.aspire.asat.phishing.dto.response.ValidationResult;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.LandingPageMapper;
import com.aspire.asat.phishing.model.Domain;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.DomainRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.HtmlSanitizerService;
import com.aspire.asat.phishing.service.HtmlValidatorService;
import com.aspire.asat.phishing.service.EmailTemplateLandingPageBindingService;
import com.aspire.asat.phishing.service.LandingPageService;
import com.aspire.asat.phishing.service.support.CatalogReferenceResolver;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of LandingPageService.
 * Handles landing page CRUD operations with permission checking.
 * Based on BRD Use Case 2.1.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LandingPageServiceImpl implements LandingPageService {
    
    private final LandingPageRepository landingPageRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final DomainRepository domainRepository;
    private final LandingPageMapper landingPageMapper;
    private final CatalogReferenceResolver catalogReferenceResolver;
    private final UserCurrentContextService userCurrentContextService;
    private final HtmlSanitizerService htmlSanitizerService;
    private final HtmlValidatorService htmlValidatorService;
    private final TrackingBaseUrlResolver trackingBaseUrlResolver;
    private final EmailTemplateLandingPageBindingService bindingService;
    
    private static final String ROLE_CLIENT_ADMIN = UserType.CLIENT_ADMIN.getValue();
    
    @Override
    public List<LandingPageDto> getLandingPages(
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            String clientId,
            int offset,
            int pageSize,
            String sortBy,
            String sortOrder) {
        
        UserType currentUserType = getCurrentUserType();
        boolean isAspireAdmin = currentUserType == UserType.ASPIRE_ADMIN || currentUserType == UserType.SYSTEM_USER || currentUserType == UserType.SUPER_ADMIN;
        String userId = getCurrentClientId();
        if (!isAspireAdmin) {
            clientId = userId;
        }

        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int pageNumber = Math.max(0, offset);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(pageNumber, effectivePageSize, sort);

        List<LandingPage> pages = landingPageRepository.findWithFilters(
                clientId,
                searchParam,
                pageType,
                categoryId,
                difficultyId,
                status,
                tags,
                isAspireAdmin,
                pageable
        );

        List<LandingPageDto> result = pages.stream()
                .map(page -> {
                    boolean canEdit = canEditPage(page, currentUserType);
                    boolean canDelete = canDeletePage(page, currentUserType);
                    return landingPageMapper.toListItemDto(page, canEdit, canDelete);
                })
                .toList();
        enrichTrackingDomains(result);
        return result;
    }
    
    @Override
    public long countLandingPages(
            String searchParam,
            LandingPageType pageType,
            String categoryId,
            String difficultyId,
            LandingPageStatus status,
            List<String> tags,
            String clientId) {
        UserType currentUserType = getCurrentUserType();
        boolean isAspireAdmin = currentUserType == UserType.ASPIRE_ADMIN || currentUserType == UserType.SYSTEM_USER || currentUserType == UserType.SUPER_ADMIN;
        String userId = getCurrentClientId();
        if (!isAspireAdmin) {
            clientId = userId;
        }

        return landingPageRepository.countWithFilters(
                clientId,
                searchParam,
                pageType,
                categoryId,
                difficultyId,
                status,
                tags,
                isAspireAdmin
        );
    }
    
    @Override
    public Optional<LandingPageDto> getLandingPageById(String pageId) {
        String clientId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        
        return landingPageRepository.findByIdAndClientIdOrGlobal(pageId, clientId)
                .map(page -> enrichWithBindings(
                        landingPageMapper.toDto(page, canEditPage(page, currentUserType), canDeletePage(page, currentUserType)),
                        page.getId()));
    }
    
    @Override
    public Optional<LandingPagePreviewDto> getLandingPagePreview(String pageId) {
        String clientId = getCurrentClientId();
        
        return landingPageRepository.findByIdAndClientIdOrGlobal(pageId, clientId)
                .map(landingPageMapper::toPreviewDto);
    }
    
    @Override
    @Transactional
    public LandingPageDto updateLandingPage(String pageId, LandingPageUpdateRequest request) {
        String clientId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        
        LandingPage page = landingPageRepository.findByIdAndClientIdOrGlobal(pageId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page not found"));
        
        // Permission rules:
        // - CLIENT_ADMIN can update only pages they created (non-global).
        // - ASPIRE_ADMIN / SUPER_ADMIN pages are visible to client admins (isGlobal=true) but read-only for them.
        if (!canEditPage(page, currentUserType)) {
            throw new ServiceException("You do not have permission to edit this landing page");
        }
        
        LandingPageCategoryDto resolvedCategory = request.getCategory() != null
                ? catalogReferenceResolver.resolveCategory(request.getCategory())
                : page.getCategory();
        DifficultyDto resolvedDifficulty = request.getDifficultyLevel() != null
                ? catalogReferenceResolver.resolveDifficulty(request.getDifficultyLevel())
                : page.getDifficultyLevel();
        DataCaptureTypeDto resolvedDataCaptureType = request.getDataCaptureType() != null
                ? catalogReferenceResolver.resolveDataCaptureType(request.getDataCaptureType())
                : page.getDataCaptureType();

        String htmlContent = request.getHtmlContent();
        if (htmlContent != null) {
            htmlContent = htmlSanitizerService.sanitize(htmlContent);
        }

        request = LandingPageUpdateRequest.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(resolvedCategory)
                .difficultyLevel(resolvedDifficulty)
                .department(request.getDepartment())
                .targetDepartment(request.getTargetDepartment())
                .targetIndustry(request.getTargetIndustry())
                .constraints(request.getConstraints())
                .urgencyLevel(request.getUrgencyLevel())
                .emotionalTrigger(request.getEmotionalTrigger())
                .dataCaptureType(resolvedDataCaptureType)
                .status(request.getStatus())
                .htmlContent(htmlContent)
                .thumbnailUrl(request.getThumbnailUrl())
                .tags(request.getTags())
                .captureSubmittedData(request.isCaptureSubmittedData())
                .captureFields(request.getCaptureFields())
                .redirectUrl(request.getRedirectUrl())
                .trackingDomainId(request.getTrackingDomainId())
                .emailTemplateIds(request.getEmailTemplateIds())
                .build();

        if (StringUtils.hasText(request.getTrackingDomainId())) {
            trackingBaseUrlResolver.validateDomainId(clientId, request.getTrackingDomainId());
        }

        landingPageMapper.updateFromRequest(page, request);
        LandingPage saved = landingPageRepository.save(page);

        if (request.getEmailTemplateIds() != null) {
            bindingService.syncTemplatesForLandingPage(pageId, request.getEmailTemplateIds(), clientId);
        }
        
        log.info("Landing page {} updated by user {}", pageId, getCurrentUserId());
        
        return enrichWithBindings(landingPageMapper.toDto(saved, true, true), saved.getId());
    }
    
    @Override
    @Transactional
    public void deleteLandingPage(String pageId) {
        String clientId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        
        LandingPage page = landingPageRepository.findByIdAndClientIdOrGlobal(pageId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page not found"));
        
        // Check delete permission (BR-03)
        if (!canDeletePage(page, currentUserType)) {
            throw new ServiceException("You do not have permission to delete this landing page");
        }
        
        // TODO: Check if page is used in active campaigns
        // if (landingPageRepository.isPageUsedInCampaign(pageId)) {
        //     throw new ServiceException("Cannot delete page. It is used by active campaigns");
        // }

        bindingService.removeLandingPageFromAllTemplates(pageId);
        
        landingPageRepository.delete(page);
        
        log.info("Landing page {} deleted by user {}", pageId, getCurrentUserId());
    }
    
    @Override
    @Transactional
    public LandingPageDto duplicateLandingPage(String pageId) {
        String clientId = getCurrentClientId();
        String userId = getCurrentUserId();
        UserType userType = getCurrentUserType();
        
        LandingPage original = landingPageRepository.findByIdAndClientIdOrGlobal(pageId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page not found"));
        
        // Create duplicate with new ownership (BR-04)
        LandingPage duplicate = landingPageMapper.createDuplicate(original, clientId, userId,
                userType != null ? userType.getValue() : null);
        
        LandingPage saved = landingPageRepository.save(duplicate);
        
        log.info("Landing page {} duplicated as {} by user {}", pageId, saved.getId(), userId);

        return enrichTrackingDomain(landingPageMapper.toDto(saved, true, true));
    }
    
    @Override
    @Transactional
    public void incrementPopularity(String pageId) {
        landingPageRepository.findById(pageId).ifPresent(page -> {
            page.setPopularity(page.getPopularity() + 1);
            landingPageRepository.save(page);
        });
    }
    
    // --- Task-05: Creation Methods ---
    
    @Override
    @Transactional
    public LandingPageDto createLandingPage(LandingPageCreateRequest request) {
        String clientId = getCurrentClientId();
        String userId = getCurrentUserId();
        UserType userType = getCurrentUserType();
        
        // Check if name already exists
        if (isPageNameExists(request.getName())) {
            throw new ServiceException("A page with this name already exists");
        }
        
        // Validate HTML content (BR-05)
        ValidationResult validation = validateHtml(request.getHtmlContent(), true, true);
        if (!validation.isValid()) {
            throw new ServiceException("HTML content contains syntax errors: " + 
                    String.join(", ", validation.getErrors()));
        }

        if (StringUtils.hasText(request.getTrackingDomainId())) {
            trackingBaseUrlResolver.validateDomainId(clientId, request.getTrackingDomainId());
        }
        
        // Sanitize HTML content
        String sanitizedHtml = htmlSanitizerService.sanitize(request.getHtmlContent());
        
        LandingPage landingPage = LandingPage.builder()
                .clientId(clientId)
                .name(request.getName())
                .description(request.getDescription())
                .pageType(request.getPageType())
                .category(catalogReferenceResolver.resolveCategory(request.getCategory()))
                .difficultyLevel(catalogReferenceResolver.resolveDifficulty(request.getDifficultyLevel()))
                .department(request.getDepartment())
                .targetDepartment(request.getTargetDepartment())
                .targetIndustry(request.getTargetIndustry())
                .constraints(request.getConstraints())
                .urgencyLevel(request.getUrgencyLevel())
                .emotionalTrigger(request.getEmotionalTrigger())
                .dataCaptureType(catalogReferenceResolver.resolveDataCaptureType(request.getDataCaptureType()))
                .status(request.getStatus() != null ? request.getStatus() : LandingPageStatus.ACTIVE)
                .htmlContent(sanitizedHtml)
                .thumbnailUrl(request.getThumbnailUrl())
                .websiteUrl(request.getWebsiteUrl())
                .tags(request.getTags() != null ? new java.util.ArrayList<>(request.getTags()) : new java.util.ArrayList<>())
                .captureSubmittedData(request.isCaptureSubmittedData())
                .captureFields(request.getCaptureFields() != null ? new java.util.ArrayList<>(request.getCaptureFields()) : new java.util.ArrayList<>())
                .redirectUrl(request.getRedirectUrl())
                .trackingDomainId(StringUtils.hasText(request.getTrackingDomainId())
                        ? request.getTrackingDomainId().trim() : null)
                .popularity(0)
                .isGlobal(isGlobalPage(userType))
                .isPremium(false)
                .templateGenerationType(request.getTemplateGenerationType())
                .createdBy(userId)
                .createdByRole(userType != null ? userType.getValue() : null)
                .build();
        
        LandingPage saved = landingPageRepository.save(landingPage);

        if (request.getEmailTemplateIds() != null && !request.getEmailTemplateIds().isEmpty()) {
            bindingService.addLandingPageToTemplates(saved.getId(), request.getEmailTemplateIds(), clientId);
        }
        
        log.info("Landing page '{}' created by user {}", saved.getName(), userId);
        
        return enrichWithBindings(landingPageMapper.toDto(saved, true, true), saved.getId());
    }
    
    @Override
    public ValidationResult validateHtml(String htmlContent, boolean validateForms, boolean securityCheck) {
        return htmlValidatorService.validateWithOptions(htmlContent, validateForms, securityCheck);
    }
    
    @Override
    public boolean isPageNameExists(String name) {
        String clientId = getCurrentClientId();
        return landingPageRepository.existsByClientIdAndName(clientId, name);
    }
    
    @Override
    @Transactional
    public String uploadThumbnail(String pageId, String thumbnailUrl) {
        String clientId = getCurrentClientId();
        UserType currentUserType = getCurrentUserType();
        
        LandingPage page = landingPageRepository.findByIdAndClientIdOrGlobal(pageId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page not found"));

        if (!canEditPage(page, currentUserType)) {
            throw new ServiceException("You do not have permission to edit this landing page");
        }
        
        page.setThumbnailUrl(thumbnailUrl);
        landingPageRepository.save(page);
        
        log.info("Thumbnail uploaded for landing page {}", pageId);
        
        return thumbnailUrl;
    }
    
    // --- Permission checking methods ---
    
    /**
     * Check if user can edit the landing page
     * BR-03: Admin can update pages (except Super Admin pages)
     */
    private boolean canEditPage(LandingPage page, UserType currentUserType) {
        if (currentUserType == null) {
            return false;
        }

        // SUPER_ADMIN / ASPIRE_ADMIN / SYSTEM_USER can edit pages they have access to (client + global).
        if (currentUserType == UserType.SUPER_ADMIN || currentUserType == UserType.ASPIRE_ADMIN || currentUserType == UserType.SYSTEM_USER) {
            return true;
        }

        // CLIENT_ADMIN can edit only pages they created and that are not global.
        if (currentUserType == UserType.CLIENT_ADMIN) {
            return !page.isGlobal()
                    && ROLE_CLIENT_ADMIN.equalsIgnoreCase(page.getCreatedByRole())
                    && getCurrentUserId().equals(page.getCreatedBy());
        }

        return false;
    }
    
    /**
     * Check if user can delete the landing page
     * BR-03: Admin can delete pages (except Super Admin pages)
     */
    private boolean canDeletePage(LandingPage page, UserType currentUserType) {
        return canEditPage(page, currentUserType);
    }
    
    // --- Helper methods ---
    
    private String getCurrentClientId() {
        return userCurrentContextService.getCurrentUserContext().getClientAdminId();
    }
    
    private String getCurrentUserId() {
        return userCurrentContextService.getCurrentUserContext().getUserId();
    }
    
    private UserType getCurrentUserType() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        try {
            return UserType.fromString(context.getUserType());
        } catch (Exception e) {
            log.warn("Unable to parse userType from context: {}", context.getUserType());
            return null;
        }
    }

    private boolean isGlobalPage(UserType userType) {
        return userType == UserType.SUPER_ADMIN || userType == UserType.ASPIRE_ADMIN || userType == UserType.SYSTEM_USER;
    }

    @Override
    public List<LandingPageDto> getBoundLandingPages(String templateId) {
        String clientId = getCurrentClientId();
        emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        return bindingService.getBoundLandingPages(templateId, clientId);
    }

    @Override
    public List<EmailTemplateDto> getBoundEmailTemplates(String pageId) {
        String clientId = getCurrentClientId();
        landingPageRepository.findByIdAndClientIdOrGlobal(pageId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page not found"));
        return bindingService.getBoundEmailTemplates(pageId, clientId);
    }

    private LandingPageDto enrichWithBindings(LandingPageDto dto, String pageId) {
        if (dto == null) {
            return null;
        }
        dto.setEmailTemplateIds(bindingService.getBoundEmailTemplateIds(pageId, getCurrentClientId()));
        return enrichTrackingDomain(dto);
    }

    private LandingPageDto enrichTrackingDomain(LandingPageDto dto) {
        if (dto != null) {
            enrichTrackingDomains(List.of(dto));
        }
        return dto;
    }

    /**
     * Resolves {@link LandingPageDto#getTrackingDomain()} from {@code domains.domain}
     * via a single batch lookup on {@code trackingDomainId}.
     */
    private void enrichTrackingDomains(List<LandingPageDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        Set<String> ids = dtos.stream()
                .map(LandingPageDto::getTrackingDomainId)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ids.isEmpty()) {
            return;
        }
        Map<String, String> hostnamesById = new HashMap<>();
        for (Domain domain : domainRepository.findAllById(ids)) {
            if (domain.getId() != null) {
                hostnamesById.put(domain.getId(), domain.getDomain());
            }
        }
        for (LandingPageDto dto : dtos) {
            if (StringUtils.hasText(dto.getTrackingDomainId())) {
                dto.setTrackingDomain(hostnamesById.get(dto.getTrackingDomainId().trim()));
            }
        }
    }
}
