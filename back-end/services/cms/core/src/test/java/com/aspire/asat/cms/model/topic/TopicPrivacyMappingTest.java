package com.aspire.asat.cms.model.topic;

import com.aspire.asat.cms.config.TopicStatusConfig;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.TopicReqDto;
import com.aspire.asat.cms.dto.topic.TopicRespDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicPrivacyMappingTest {

    @Test
    void toTopic_withClientId_setsPrivateAndHonorsStatus() {
        TopicReqDto dto = TopicReqDto.builder()
                .topicName("Private Topic")
                .categoryIds(List.of("cat-1"))
                .countryIds(List.of("c-1"))
                .complianceIds(List.of("comp-1"))
                .clientId("client-abc")
                .status(TopicStatus.ENABLED)
                .build();

        Topic topic = Topic.toTopic("id-1", dto, Instant.now(), Instant.now());

        assertEquals("client-abc", topic.getClientId());
        assertTrue(topic.getIsPrivate());
        assertEquals(TopicStatus.ENABLED, topic.getStatus());
    }

    @Test
    void toTopic_withoutClientId_isPublicAndDefaultsDisabled() {
        TopicReqDto dto = TopicReqDto.builder()
                .topicName("Public Topic")
                .categoryIds(List.of("cat-1"))
                .countryIds(List.of("c-1"))
                .complianceIds(List.of("comp-1"))
                .build();

        Topic topic = Topic.toTopic("id-2", dto, Instant.now(), Instant.now());

        assertNull(topic.getClientId());
        assertFalse(topic.getIsPrivate());
        assertEquals(TopicStatusConfig.DEFAULT_TOPIC_STATUS, topic.getStatus());
        assertEquals(TopicStatus.DISABLED, topic.getStatus());
    }

    @Test
    void toTopic_withoutClientId_ignoresRequestStatus_keepsDisabled() {
        // Existing CMS admin creates must remain DISABLED even if the UI sends ENABLED.
        TopicReqDto dto = TopicReqDto.builder()
                .topicName("Public Topic")
                .categoryIds(List.of("cat-1"))
                .countryIds(List.of("c-1"))
                .complianceIds(List.of("comp-1"))
                .status(TopicStatus.ENABLED)
                .build();

        Topic topic = Topic.toTopic("id-2b", dto, Instant.now(), Instant.now());

        assertNull(topic.getClientId());
        assertFalse(topic.getIsPrivate());
        assertEquals(TopicStatus.DISABLED, topic.getStatus());
    }

    @Test
    void toTopicRespDto_mapsPrivacyFields() {
        Topic topic = Topic.builder()
                .id("id-3")
                .topicName("Mapped")
                .clientId("client-x")
                .isPrivate(true)
                .status(TopicStatus.ENABLED)
                .build();

        TopicRespDto resp = Topic.toTopicRespDto(topic);

        assertEquals("client-x", resp.getClientId());
        assertTrue(resp.getIsPrivate());
    }
}
