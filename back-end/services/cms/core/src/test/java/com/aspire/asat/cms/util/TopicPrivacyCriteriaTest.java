package com.aspire.asat.cms.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicPrivacyCriteriaTest {

    @Test
    void isVisible_publicTopicsAlwaysVisible() {
        assertTrue(TopicPrivacyCriteria.isVisible(null, null, "client-1"));
        assertTrue(TopicPrivacyCriteria.isVisible(false, null, "client-1"));
    }

    @Test
    void isVisible_privateTopic_matchingClient() {
        assertTrue(TopicPrivacyCriteria.isVisible(true, "client-1", "client-1"));
    }

    @Test
    void isVisible_privateTopic_otherClientHidden() {
        assertFalse(TopicPrivacyCriteria.isVisible(true, "client-1", "client-2"));
    }

    @Test
    void isVisible_privateTopic_noViewerContext_seeAll() {
        assertTrue(TopicPrivacyCriteria.isVisible(true, "client-1", null));
        assertTrue(TopicPrivacyCriteria.isVisible(true, "client-1", "  "));
    }

    @Test
    void isVisible_legacyTopicsWithoutPrivacyFields_arePublic() {
        // Existing Mongo docs with missing isPrivate/clientId must remain visible to all clients.
        assertTrue(TopicPrivacyCriteria.isVisible(null, null, "client-1"));
        assertTrue(TopicPrivacyCriteria.isVisible(null, null, null));
    }
}
