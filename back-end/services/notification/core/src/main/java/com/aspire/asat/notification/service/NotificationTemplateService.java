package com.aspire.asat.notification.service;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.repository.NotificationTemplateRepository;
import com.aspire.asat.notification.service.support.NotificationTemplateRoleMatrix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing notification templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateService {
    
    private final NotificationTemplateRepository repository;
    private final TemplateEngine templateEngine;
    
    /**
     * Get template by ID
     */
    public Optional<NotificationTemplate> getTemplateById(String templateId) {
        return repository.findById(templateId);
    }
    
    /**
     * Get template for notification type and channel.
     *
     * <p>The role-agnostic base template wins over any role variant sharing the same type and
     * channel, so the fallback used for a role without its own template is deterministic.
     * Data predating role variants may have no base row, in which case any active template is
     * returned as before.
     */
    public Optional<NotificationTemplate> getTemplate(NotificationType notificationType, NotificationChannel channel) {
        Optional<NotificationTemplate> base = repository
                .findByNotificationTypeAndChannelAndRecipientRoleAndIsActiveTrue(notificationType, channel, null);
        return base.isPresent()
                ? base
                : repository.findByNotificationTypeAndChannelAndIsActiveTrue(notificationType, channel);
    }
    
    /**
     * Get template for notification type, channel and organization
     */
    public Optional<NotificationTemplate> getTemplate(NotificationType notificationType, NotificationChannel channel, String organizationId) {
        if (organizationId != null) {
            return repository.findByNotificationTypeAndChannelAndOrganizationIdAndIsActiveTrue(
                notificationType, channel, organizationId);
        }
        return getTemplate(notificationType, channel);
    }
    
    /**
     * Get template for notification type, channel and recipient role. Tries the role-specific
     * template first, then falls back to the role-agnostic template so existing templates
     * (all with a null role) keep working untouched. When {@code role} is null, behaves exactly
     * like {@link #getTemplate(NotificationType, NotificationChannel)}.
     */
    public Optional<NotificationTemplate> getTemplate(NotificationType notificationType, NotificationChannel channel,
                                                        NotificationRecipientRole role) {
        if (role == null) {
            return getTemplate(notificationType, channel);
        }
        Optional<NotificationTemplate> roleSpecific =
                repository.findByNotificationTypeAndChannelAndRecipientRoleAndIsActiveTrue(notificationType, channel, role);
        return roleSpecific.isPresent() ? roleSpecific : getTemplate(notificationType, channel);
    }

    /**
     * Get default template for notification type and channel
     */
    public Optional<NotificationTemplate> getDefaultTemplate(NotificationType notificationType, NotificationChannel channel) {
        return repository.findByNotificationTypeAndChannelAndIsDefaultTrue(notificationType, channel);
    }
    
    /**
     * Get all templates for a notification type
     */
    public List<NotificationTemplate> getTemplatesByType(NotificationType notificationType) {
        return repository.findByNotificationTypeAndIsActiveTrue(notificationType);
    }
    
    /**
     * Get all templates for a channel
     */
    public List<NotificationTemplate> getTemplatesByChannel(NotificationChannel channel) {
        return repository.findByChannel(channel);
    }
    
    /**
     * Get templates for priority notification types
     */
    public List<NotificationTemplate> getPriorityTemplates() {
        return repository.findByPriorityTypes(NotificationType.PRIORITY_TYPES);
    }

    /**
     * Get all templates with optional filters and pagination.
     *
     * @param roleFilterPresent when true, {@code recipientRole} is applied as a filter even when
     *                          null, which selects only the role-agnostic base templates
     */
    public List<NotificationTemplate> getAllTemplates(int offset,
                                                      int pageSize,
                                                      NotificationType notificationType,
                                                      NotificationChannel channel,
                                                      NotificationRecipientRole recipientRole,
                                                      boolean roleFilterPresent) {
        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 20 : pageSize;

        if (roleFilterPresent && recipientRole != null) {
            List<NotificationTemplate> effective = resolveEffectiveTemplatesForRole(
                    notificationType, channel, recipientRole);
            return pageList(effective, safeOffset, safePageSize);
        }

        PageRequest pageRequest = PageRequest.of(safeOffset, safePageSize);
        return repository.search(notificationType, channel, recipientRole, roleFilterPresent, pageRequest);
    }

    /**
     * Count templates matching the same optional filters as
     * {@link #getAllTemplates(int, int, NotificationType, NotificationChannel, NotificationRecipientRole, boolean)}.
     */
    public long countAllTemplates(NotificationType notificationType,
                                  NotificationChannel channel,
                                  NotificationRecipientRole recipientRole,
                                  boolean roleFilterPresent) {
        if (roleFilterPresent && recipientRole != null) {
            return resolveEffectiveTemplatesForRole(notificationType, channel, recipientRole).size();
        }
        return repository.count(notificationType, channel, recipientRole, roleFilterPresent);
    }

    /**
     * Templates an admin should see when filtering by a concrete recipient role:
     * role-specific variants, plus base templates for any (type, channel) that has no variant
     * for that role (so inheriting roles still surface an editable template).
     */
    private List<NotificationTemplate> resolveEffectiveTemplatesForRole(NotificationType notificationType,
                                                                        NotificationChannel channel,
                                                                        NotificationRecipientRole recipientRole) {
        List<NotificationTemplate> roleVariants =
                repository.search(notificationType, channel, recipientRole, true, null);
        List<NotificationTemplate> baseTemplates =
                repository.search(notificationType, channel, null, true, null);

        Set<String> coveredTypeChannels = new HashSet<>();
        for (NotificationTemplate variant : roleVariants) {
            coveredTypeChannels.add(typeChannelKey(variant));
        }

        List<NotificationTemplate> effective = new ArrayList<>(roleVariants);
        for (NotificationTemplate base : baseTemplates) {
            if (!coveredTypeChannels.contains(typeChannelKey(base))) {
                effective.add(base);
            }
        }

        effective.sort(Comparator
                .comparing(NotificationTemplate::getNotificationType, Comparator.nullsLast(Enum::compareTo))
                .thenComparing(NotificationTemplate::getChannel, Comparator.nullsLast(Enum::compareTo))
                .thenComparing(template -> template.getRecipientRole() == null ? 0 : 1)
                .thenComparing(NotificationTemplate::getId, Comparator.nullsLast(String::compareTo)));
        return effective;
    }

    private static String typeChannelKey(NotificationTemplate template) {
        return Objects.toString(template.getNotificationType(), "")
                + "|"
                + Objects.toString(template.getChannel(), "");
    }

    private static List<NotificationTemplate> pageList(List<NotificationTemplate> items, int offset, int pageSize) {
        if (offset >= items.size()) {
            return List.of();
        }
        int end = Math.min(offset + pageSize, items.size());
        return items.subList(offset, end);
    }
    
    /**
     * Generate HTML content from template
     */
    public String generateHtmlContent(NotificationTemplate template, Map<String, Object> model) {
        if (template == null || template.getHtmlTemplate() == null) {
            log.warn("Template or HTML template is null");
            return "";
        }
        
        try {
            String htmlContent = template.getHtmlTemplate();
            
            // Replace template variables with actual values
            if (model != null && !model.isEmpty()) {
                htmlContent = replaceTemplateVariables(htmlContent, model);
            }
            
            return htmlContent;
        } catch (Exception e) {
            log.error("Error generating HTML content for template {}: {}", template.getId(), e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Generate subject from template
     */
    public String generateSubject(NotificationTemplate template, Map<String, Object> model) {
        return processTemplate(template.getSubjectTemplate(), model);
    }
    
    /**
     * Generate title from template
     */
    public String generateTitle(NotificationTemplate template, Map<String, Object> model) {
        return processTemplate(template.getTitleTemplate(), model);
    }
    
    /**
     * Generate message from template
     */
    public String generateMessage(NotificationTemplate template, Map<String, Object> model) {
        return processTemplate(template.getMessageTemplate(), model);
    }
    
    /**
     * Generate text content from template
     */
    public String generateTextContent(NotificationTemplate template, Map<String, Object> model) {
        return processTemplate(template.getTextTemplate(), model);
    }
    
    /**
     * Process a template string with model data
     */
    private String processTemplate(String template, Map<String, Object> model) {
        if (template == null || template.trim().isEmpty()) {
            return "";
        }
        
        try {
            String processedTemplate = template;
            
            // Replace template variables with actual values
            if (model != null && !model.isEmpty()) {
                processedTemplate = replaceTemplateVariables(template, model);
            }
            
            return processedTemplate;
        } catch (Exception e) {
            log.error("Error processing template: {}", e.getMessage(), e);
            return template; // Return the original template if processing fails
        }
    }
    
    /**
     * Replace template variables ({{variableName}}) with actual values from model
     */
    private String replaceTemplateVariables(String template, Map<String, Object> model) {
        String result = template;
        
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    /**
     * Create a new template with single active constraint
     */
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        template.setId(java.util.UUID.randomUUID().toString());
        
        // If this template is being set as active, deactivate all others for the same type, channel and role
        if (template.isActive()) {
            deactivateAllOtherTemplates(template.getNotificationType(), template.getChannel(), template.getRecipientRole());
        }
        
        NotificationTemplate saved = repository.save(template);
        log.info("Created template {} for notification type {} and channel {}", 
            saved.getId(), template.getNotificationType(), template.getChannel());
        return saved;
    }
    
    /**
     * Create a role-specific variant of an existing base template.
     *
     * <p>A variant only makes sense for a notification type and channel the platform already
     * sends, so the role-agnostic base template must exist. Content fields left null on
     * {@code contentOverrides} are copied from the base, giving the admin an editable starting
     * point instead of an empty template.
     *
     * @param contentOverrides carries template name, channel content and organization id only;
     *                         its type, channel and role are ignored
     * @throws IllegalArgumentException when the role is missing, the base template does not exist
     *                                  or a variant for that role already exists
     */
    public NotificationTemplate createRoleVariant(NotificationType notificationType,
                                                  NotificationChannel channel,
                                                  NotificationRecipientRole recipientRole,
                                                  NotificationTemplate contentOverrides) {
        if (notificationType == null || channel == null) {
            throw new IllegalArgumentException("Notification type and channel are required to create a role variant");
        }
        if (recipientRole == null) {
            throw new IllegalArgumentException(
                    "Recipient role is required; the role-agnostic base template cannot be created");
        }

        NotificationTemplate base = getBaseTemplate(notificationType, channel)
                .orElseThrow(() -> new IllegalArgumentException("No base template exists for notification type "
                        + notificationType + " and channel " + channel));

        List<NotificationTemplate> existingVariants =
                repository.findAllByNotificationTypeAndChannelAndRecipientRole(notificationType, channel, recipientRole);
        if (!existingVariants.isEmpty()) {
            throw new IllegalArgumentException("A " + recipientRole + " template already exists for notification type "
                    + notificationType + " and channel " + channel + " (id: " + existingVariants.get(0).getId()
                    + "); update it instead");
        }

        NotificationTemplate overrides = contentOverrides == null ? new NotificationTemplate() : contentOverrides;
        NotificationTemplate variant = NotificationTemplate.builder()
                .notificationType(notificationType)
                .channel(channel)
                .recipientRole(recipientRole)
                .templateName(valueOrFallback(overrides.getTemplateName(),
                        base.getTemplateName() + " (" + recipientRole + ")"))
                .subjectTemplate(valueOrFallback(overrides.getSubjectTemplate(), base.getSubjectTemplate()))
                .htmlTemplate(valueOrFallback(overrides.getHtmlTemplate(), base.getHtmlTemplate()))
                .textTemplate(valueOrFallback(overrides.getTextTemplate(), base.getTextTemplate()))
                .titleTemplate(valueOrFallback(overrides.getTitleTemplate(), base.getTitleTemplate()))
                .messageTemplate(valueOrFallback(overrides.getMessageTemplate(), base.getMessageTemplate()))
                .isActive(true)
                .isDefault(false)
                .organizationId(overrides.getOrganizationId())
                .build();

        NotificationTemplate saved = createTemplate(variant);
        log.info("Created {} role variant {} from base template {} for notification type {} and channel {}",
                recipientRole, saved.getId(), base.getId(), notificationType, channel);
        return saved;
    }

    /**
     * Delete a role-specific template so that role falls back to the base template again.
     *
     * @throws IllegalArgumentException when the template does not exist or is a base template,
     *                                  which every role without a variant depends on
     */
    public void deleteRoleVariant(String templateId) {
        NotificationTemplate template = repository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));

        if (template.getRecipientRole() == null) {
            throw new IllegalArgumentException("Base templates cannot be deleted because every role without a "
                    + "variant falls back to them; only role variants can be removed");
        }

        repository.deleteById(templateId);
        log.info("Deleted {} role variant {} for notification type {} and channel {}",
                template.getRecipientRole(), templateId, template.getNotificationType(), template.getChannel());
    }

    /**
     * Get the base template and every role variant configured for a notification type and channel,
     * so an admin panel can show which roles are customized and which inherit the base.
     */
    public NotificationTemplateRoleMatrix getRoleMatrix(NotificationType notificationType, NotificationChannel channel) {
        if (notificationType == null || channel == null) {
            throw new IllegalArgumentException("Notification type and channel are required to resolve the role matrix");
        }

        List<NotificationTemplate> templates =
                repository.findAllByNotificationTypeAndChannel(notificationType, channel);

        NotificationTemplate base = templates.stream()
                .filter(template -> template.getRecipientRole() == null)
                .filter(NotificationTemplate::isActive)
                .findFirst()
                .orElseGet(() -> templates.stream()
                        .filter(template -> template.getRecipientRole() == null)
                        .findFirst()
                        .orElse(null));

        Map<NotificationRecipientRole, NotificationTemplate> variants = new EnumMap<>(NotificationRecipientRole.class);
        for (NotificationTemplate template : templates) {
            NotificationRecipientRole role = template.getRecipientRole();
            if (role == null) {
                continue;
            }
            NotificationTemplate current = variants.get(role);
            if (current == null || (!current.isActive() && template.isActive())) {
                variants.put(role, template);
            }
        }

        return new NotificationTemplateRoleMatrix(base, variants);
    }

    /**
     * Get the role-agnostic base template for a notification type and channel, active or not.
     */
    public Optional<NotificationTemplate> getBaseTemplate(NotificationType notificationType, NotificationChannel channel) {
        List<NotificationTemplate> baseTemplates =
                repository.findAllByNotificationTypeAndChannelAndRecipientRole(notificationType, channel, null);
        return baseTemplates.stream()
                .filter(NotificationTemplate::isActive)
                .findFirst()
                .or(() -> baseTemplates.stream().findFirst());
    }

    private static String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * Update an existing template with single active constraint
     */
    public NotificationTemplate updateTemplate(NotificationTemplate template) {
        // If this template is being set as active, deactivate all others for the same type, channel and role
        if (template.isActive()) {
            deactivateAllOtherTemplates(template.getNotificationType(), template.getChannel(), template.getRecipientRole());
        }
        
        NotificationTemplate saved = repository.save(template);
        log.info("Updated template {} for notification type {} and channel {}", 
            saved.getId(), template.getNotificationType(), template.getChannel());
        return saved;
    }
    
    /**
     * Deactivate all other templates for the same notification type, channel and recipient role
     */
    private void deactivateAllOtherTemplates(NotificationType notificationType, NotificationChannel channel, NotificationRecipientRole role) {
        List<NotificationTemplate> templates = repository.findAllByNotificationTypeAndChannelAndRecipientRole(notificationType, channel, role);
        for (NotificationTemplate template : templates) {
            if (template.isActive()) {
                template.setActive(false);
                repository.save(template);
                log.info("Deactivated template {} for notification type {}, channel {} and role {}",
                    template.getId(), notificationType, channel, role);
            }
        }
    }
    
    /**
     * Activate a template (deactivates all others for the same type, channel and role)
     */
    public NotificationTemplate activateTemplate(String templateId) {
        Optional<NotificationTemplate> templateOpt = repository.findById(templateId);
        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            
            // Deactivate all other templates for the same type, channel and role
            deactivateAllOtherTemplates(template.getNotificationType(), template.getChannel(), template.getRecipientRole());
            
            // Activate this template
            template.setActive(true);
            NotificationTemplate saved = repository.save(template);
            log.info("Activated template {} for notification type {} and channel {}",
                saved.getId(), template.getNotificationType(), template.getChannel());
            return saved;
        }
        return null;
    }
    
    /**
     * Set template as default (activates it and deactivates others)
     */
    public NotificationTemplate setTemplateAsDefault(String templateId) {
        Optional<NotificationTemplate> templateOpt = repository.findById(templateId);
        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            
            // Deactivate all other templates for the same type, channel and role
            deactivateAllOtherTemplates(template.getNotificationType(), template.getChannel(), template.getRecipientRole());
            
            // Set this template as default and active
            template.setDefault(true);
            template.setActive(true);
            NotificationTemplate saved = repository.save(template);
            log.info("Set template {} as default for notification type {} and channel {}",
                saved.getId(), template.getNotificationType(), template.getChannel());
            return saved;
        }
        return null;
    }
    
    /**
     * Deactivate a template
     */
    public void deactivateTemplate(String templateId) {
        Optional<NotificationTemplate> templateOpt = repository.findById(templateId);
        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            template.setActive(false);
            repository.save(template);
            log.info("Deactivated template {} for notification type {} and channel {}", 
                templateId, template.getNotificationType(), template.getChannel());
        } else {
            log.warn("Template not found for deactivation: {}", templateId);
        }
    }
}
