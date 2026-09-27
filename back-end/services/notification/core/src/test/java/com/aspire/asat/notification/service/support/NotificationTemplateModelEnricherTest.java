package com.aspire.asat.notification.service.support;

import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.notification.NotificationTemplateValue;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationTemplateModelEnricherTest {

    private final NotificationTemplateModelEnricher enricher = new NotificationTemplateModelEnricher();

    @Test
    void mergeFormatsEventTimestampUsingOffsetLabel() {
        Map<String, Object> eventModel = new HashMap<>();
        eventModel.put(NotificationTemplateValue.EVENT_TIMESTAMP, "2026-08-23T14:26:09Z");
        eventModel.put(NotificationTemplateValue.TIMESTAMP, "ignored-preformatted");

        NotificationRecipientBundleDto bundle = NotificationRecipientBundleDto.builder()
                .organizationTimezoneLabel("SGT (UTC+08:00)")
                .organizationTimezoneDisplayName("Singapore Time")
                .userFullName("Robert Bruse")
                .build();

        NotificationRecipientTarget target = new NotificationRecipientTarget(
                "user-1", "Robert Bruse", "user@example.com", null, "ca-1");

        Map<String, Object> merged = enricher.merge(
                eventModel, null, NotificationRecipientRole.USER, bundle, target);

        assertEquals("23 August 2026 at 10:26 PM", merged.get(NotificationTemplateValue.TIMESTAMP));
    }

    @Test
    void mergeLeavesTimestampWhenEventTimestampMissing() {
        Map<String, Object> eventModel = new HashMap<>();
        eventModel.put(NotificationTemplateValue.TIMESTAMP, "23 August 2026 at 02:26 PM");

        Map<String, Object> merged = enricher.merge(
                eventModel, null, NotificationRecipientRole.USER, null, null);

        assertEquals("23 August 2026 at 02:26 PM", merged.get(NotificationTemplateValue.TIMESTAMP));
    }
}
