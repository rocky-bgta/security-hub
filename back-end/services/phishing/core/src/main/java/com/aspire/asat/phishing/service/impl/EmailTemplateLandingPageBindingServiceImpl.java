package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.mapper.EmailTemplateMapper;
import com.aspire.asat.phishing.mapper.LandingPageMapper;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.EmailTemplateLandingPageBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateLandingPageBindingServiceImpl implements EmailTemplateLandingPageBindingService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final LandingPageRepository landingPageRepository;
    private final EmailTemplateMapper emailTemplateMapper;
    private final LandingPageMapper landingPageMapper;

    @Override
    @Transactional
    public void setLandingPagesForTemplate(String templateId, List<String> landingPageIds, String clientId) {
        EmailTemplate template = emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        List<String> normalizedIds = normalizeIds(landingPageIds);
        validateLandingPageIds(normalizedIds, clientId);

        template.setLandingPageIds(new ArrayList<>(normalizedIds));
        emailTemplateRepository.save(template);
        log.debug("Set {} landing page binding(s) on template {}", normalizedIds.size(), templateId);
    }

    @Override
    @Transactional
    public void addLandingPageToTemplates(String landingPageId, List<String> emailTemplateIds, String clientId) {
        if (!StringUtils.hasText(landingPageId) || emailTemplateIds == null || emailTemplateIds.isEmpty()) {
            return;
        }

        landingPageRepository.findByIdAndClientIdOrGlobal(landingPageId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page not found"));

        List<String> normalizedTemplateIds = normalizeIds(emailTemplateIds);
        for (String templateId : normalizedTemplateIds) {
            EmailTemplate template = emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));

            List<String> currentIds = template.getLandingPageIds() != null
                    ? new ArrayList<>(template.getLandingPageIds())
                    : new ArrayList<>();
            if (!currentIds.contains(landingPageId)) {
                currentIds.add(landingPageId);
                template.setLandingPageIds(currentIds);
                emailTemplateRepository.save(template);
            }
        }
        log.debug("Added landing page {} to {} template binding(s)", landingPageId, normalizedTemplateIds.size());
    }

    @Override
    @Transactional
    public void syncTemplatesForLandingPage(String landingPageId, List<String> emailTemplateIds, String clientId) {
        landingPageRepository.findByIdAndClientIdOrGlobal(landingPageId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Landing page not found"));

        List<String> desiredTemplateIds = normalizeIds(emailTemplateIds);
        validateEmailTemplateIds(desiredTemplateIds, clientId);

        Set<String> desiredSet = new LinkedHashSet<>(desiredTemplateIds);

        List<EmailTemplate> currentlyBound = emailTemplateRepository
                .findByLandingPageIdsContaining(landingPageId);

        for (EmailTemplate template : currentlyBound) {
            if (!desiredSet.contains(template.getId())) {
                removeLandingPageId(template, landingPageId);
                emailTemplateRepository.save(template);
            }
        }

        for (String templateId : desiredTemplateIds) {
            EmailTemplate template = emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
            List<String> currentIds = template.getLandingPageIds() != null
                    ? new ArrayList<>(template.getLandingPageIds())
                    : new ArrayList<>();
            if (!currentIds.contains(landingPageId)) {
                currentIds.add(landingPageId);
                template.setLandingPageIds(currentIds);
                emailTemplateRepository.save(template);
            }
        }
        log.debug("Synced landing page {} bindings to {} template(s)", landingPageId, desiredTemplateIds.size());
    }

    @Override
    @Transactional
    public void removeLandingPageFromAllTemplates(String landingPageId) {
        List<EmailTemplate> boundTemplates = emailTemplateRepository.findByLandingPageIdsContaining(landingPageId);
        for (EmailTemplate template : boundTemplates) {
            removeLandingPageId(template, landingPageId);
            emailTemplateRepository.save(template);
        }
        if (!boundTemplates.isEmpty()) {
            log.debug("Removed landing page {} from {} template binding(s)", landingPageId, boundTemplates.size());
        }
    }

    @Override
    public List<LandingPageDto> getBoundLandingPages(String templateId, String clientId) {
        EmailTemplate template = emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        List<String> landingPageIds = template.getLandingPageIds();
        if (landingPageIds == null || landingPageIds.isEmpty()) {
            return List.of();
        }

        Map<String, LandingPage> pagesById = landingPageRepository.findAllById(landingPageIds).stream()
                .filter(page -> isAccessible(page.getClientId(), page.isGlobal(), clientId))
                .collect(Collectors.toMap(LandingPage::getId, Function.identity(), (a, b) -> a));

        return landingPageIds.stream()
                .map(pagesById::get)
                .filter(Objects::nonNull)
                .map(page -> landingPageMapper.toDto(page, false, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<EmailTemplateDto> getBoundEmailTemplates(String landingPageId, String clientId) {
        return emailTemplateRepository
                .findByLandingPageIdsContainingAndClientIdOrGlobal(landingPageId, clientId)
                .stream()
                .map(template -> emailTemplateMapper.toListItemDto(template, false, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getBoundEmailTemplateIds(String landingPageId, String clientId) {
        return emailTemplateRepository
                .findByLandingPageIdsContainingAndClientIdOrGlobal(landingPageId, clientId)
                .stream()
                .map(EmailTemplate::getId)
                .collect(Collectors.toList());
    }

    private void validateLandingPageIds(List<String> landingPageIds, String clientId) {
        for (String pageId : landingPageIds) {
            landingPageRepository.findByIdAndClientIdOrGlobal(pageId, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Landing page not found: " + pageId));
        }
    }

    private void validateEmailTemplateIds(List<String> emailTemplateIds, String clientId) {
        for (String templateId : emailTemplateIds) {
            emailTemplateRepository.findByIdAndClientIdOrGlobal(templateId, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + templateId));
        }
    }

    private List<String> normalizeIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String id : ids) {
            if (StringUtils.hasText(id)) {
                seen.add(id.trim());
            }
        }
        return new ArrayList<>(seen);
    }

    private void removeLandingPageId(EmailTemplate template, String landingPageId) {
        if (template.getLandingPageIds() == null) {
            return;
        }
        List<String> updated = template.getLandingPageIds().stream()
                .filter(id -> !landingPageId.equals(id))
                .collect(Collectors.toCollection(ArrayList::new));
        template.setLandingPageIds(updated);
    }

    private boolean isAccessible(String entityClientId, boolean isGlobal, String clientId) {
        return isGlobal || Objects.equals(entityClientId, clientId);
    }
}
