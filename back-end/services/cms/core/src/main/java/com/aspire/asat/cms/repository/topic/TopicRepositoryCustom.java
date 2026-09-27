package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.dto.topic.ProductPackagePairDto;
import com.aspire.asat.cms.dto.topic.TopicMinimalDto;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TopicRepositoryCustom {
    /**
     * Retrieves the full hierarchy of a topic for a specific user. This includes topic details,
     * chapters, contents, content statuses, and user progress.
     *
     * @param userId the ID of the user for whom the topic hierarchy is being fetched
     * @param topicId the ID of the topic whose hierarchy is being fetched
     * @param subpackageId the ID of the subpackage context
     * @return a Document containing the full topic hierarchy, including topic details,
     *         chapters, contents, related statuses, and user topic progress
     */
    Document getFullTopicHierarchy(String userId, String topicId, String subpackageId);

    /**
     * Finds topics by productId and packageId with optional search, pagination and sorting.
     * Returns minimal topic information (name, description, duration) for ENABLED topics only.
     *
     * @param productId the product ID (required)
     * @param packageId the package ID (required)
     * @param search optional search text to filter by topic name or description
     * @param pageable pagination and sorting information (used for sorting and initial page fetch)
     * @param offset the actual offset for offset-based pagination
     * @param pageSize the page size
     * @param viewerClientId optional viewer client id for privacy scoping; null = see all
     * @return Page of TopicMinimalDto containing only enabled topics matching the criteria
     */
    Page<TopicMinimalDto> findTopicsByProductAndPackage(String productId, String packageId, String search,
                                                        Pageable pageable, int offset, int pageSize,
                                                        String viewerClientId);

    /**
     * Finds ENABLED topics matching any of the given productId/packageId pairs.
     */
    Page<TopicMinimalDto> findTopicsByProductPackagePairs(
            List<ProductPackagePairDto> productPackages,
            String search,
            Pageable pageable,
            int offset,
            int pageSize);

}
