package com.aspire.asat.notification.utils;

import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.repository.NotificationTemplateRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Seeds notification templates on startup without overwriting existing records.
 *
 * <p>Templates updated from the admin panel must never be replaced by hardcoded seed
 * content on service restart. This sync only inserts templates that are missing for a
 * given {@code notificationType + channel} pair.
 */
@Slf4j
public final class NotificationTemplateSeedSync {

    private NotificationTemplateSeedSync() {
    }

    public record Result(int inserted, int preserved) {
    }

    /**
     * Inserts seed templates that do not yet exist. Existing templates are left unchanged.
     */
    public static Result syncInsertOnly(NotificationTemplateRepository repository,
                                        List<NotificationTemplate> seedTemplates) {
        Map<String, NotificationTemplate> existingByKey = repository.findAll().stream()
                .collect(Collectors.toMap(
                        NotificationTemplateSeedSync::templateKey,
                        template -> template,
                        (existing, replacement) -> existing
                ));

        int inserted = 0;
        int preserved = 0;

        for (NotificationTemplate seedTemplate : seedTemplates) {
            String key = templateKey(seedTemplate);
            if (existingByKey.containsKey(key)) {
                preserved++;
                log.debug("Preserving existing template for type: {} channel: {}",
                        seedTemplate.getNotificationType(), seedTemplate.getChannel());
                continue;
            }

            repository.save(seedTemplate);
            inserted++;
            log.info("Initialized new template for type: {} channel: {}",
                    seedTemplate.getNotificationType(), seedTemplate.getChannel());
        }

        return new Result(inserted, preserved);
    }

    static String templateKey(NotificationTemplate template) {
        return template.getNotificationType() + ":" + template.getChannel() + ":" + template.getRecipientRole();
    }
}
