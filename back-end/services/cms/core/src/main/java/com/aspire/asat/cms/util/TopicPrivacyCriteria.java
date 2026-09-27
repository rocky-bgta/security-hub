package com.aspire.asat.cms.util;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

/**
 * Mongo criteria for topic privacy: public topics plus private topics owned by the viewer.
 * When {@code viewerClientId} is blank, no filter is added (admin/internal see-all).
 */
public final class TopicPrivacyCriteria {

    private TopicPrivacyCriteria() {
    }

    /**
     * Appends privacy visibility criteria when a viewer client id is present.
     *
     * <pre>
     * isPrivate != true  OR  (isPrivate == true AND clientId == viewerClientId)
     * </pre>
     */
    public static void appendIfViewerPresent(java.util.List<Criteria> criteriaList, String viewerClientId) {
        if (!StringUtils.hasText(viewerClientId)) {
            return;
        }
        String clientId = viewerClientId.trim();
        criteriaList.add(new Criteria().orOperator(
                Criteria.where("isPrivate").ne(true),
                new Criteria().andOperator(
                        Criteria.where("isPrivate").is(true),
                        Criteria.where("clientId").is(clientId)
                )
        ));
    }

    /**
     * @return true if the topic is visible to the viewer (or when viewer has no client context)
     */
    public static boolean isVisible(Boolean isPrivate, String topicClientId, String viewerClientId) {
        if (!Boolean.TRUE.equals(isPrivate)) {
            return true;
        }
        if (!StringUtils.hasText(viewerClientId)) {
            return true;
        }
        return viewerClientId.trim().equals(topicClientId);
    }
}
