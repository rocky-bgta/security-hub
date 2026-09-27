package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import com.aspire.asat.cms.dto.topic.ProductPackagePairDto;
import com.aspire.asat.cms.model.topic.Topic;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public interface TopicFilterRepositoryCustom {
    Page<Topic> findByFilters(String packageId, TopicFilterRequest filterRequest);
    Page<Topic> findByFiltersByProduct(String productId, TopicFilterRequest filterRequest);
    Page<Topic> findByFiltersUnscoped(TopicFilterRequest filterRequest);

    /**
     * Soft-match recommendation: hard-scoped topics ranked by how many request criteria match.
     * Unmatched soft criteria never exclude a topic.
     */
    Page<RecommendedTopic> recommendByProduct(String productId, TopicFilterRequest filterRequest);

    Page<RecommendedTopic> recommendByPackage(String packageId, TopicFilterRequest filterRequest);

    Page<RecommendedTopic> recommendUnscoped(TopicFilterRequest filterRequest);

    /**
     * Topic plus soft-match score from recommendation aggregation.
     */
    record RecommendedTopic(Topic topic, int matchScore) {}

    /**
     * Filter topics matching (or excluding) any of the given productId/packageId pairs.
     * Also returns the total count for both the matching and non-matching scopes.
     * When {@code productPackages} is null/empty, assigned total is 0 and locked total is all topics.
     *
     * @param productPackages product/package pairs to match against (optional)
     * @param filterRequest   optional filters (status, search, metadata, pagination)
     * @param excludeMatching when true, page items from the non-matching (locked) set
     */
    ProductPackageTopicsPage findByProductPackagePairsWithTotals(
            List<ProductPackagePairDto> productPackages,
            TopicFilterRequest filterRequest,
            boolean excludeMatching);

    /**
     * Result of a product/package topic query including both assigned and locked totals.
     */
    record ProductPackageTopicsPage(Page<Topic> page, long assignedTotal, long lockedTotal) {}

    Long countTopicsBySubPackage(String subPackageId);
    
    /**
     * Count unique topics based on productIds and packageIds
     * A topic matches if at least one productPackageMapping has:
     * - productId in the productIds list AND
     * - packageIds array contains any of the packageIds
     * Only considers ENABLED topics.
     * 
     * @param productIds list of product IDs (can be empty)
     * @param packageIds list of package IDs (can be empty)
     * @return count of unique topics matching the criteria
     */
    Long countUniqueTopicsByProductAndPackage(List<String> productIds, List<String> packageIds);

    /**
     * Count ENABLED topics matching any of the given productId/packageId pairs
     * via productPackageMappings elemMatch.
     *
     * @param productPackages product/package pairs (null/empty → 0)
     * @return count of matching ENABLED topics
     */
    long countTopicsByProductPackagePairs(List<ProductPackagePairDto> productPackages);

    /**
     * Count ENABLED topics matching any of the given productId/packageId pairs,
     * grouped by month of {@code createdAt} within {@code [yearStart, yearEnd]}.
     *
     * @return map of month (1-12) → count
     */
    Map<Integer, Long> countTopicsByProductPackagePairsByMonth(
            List<ProductPackagePairDto> productPackages,
            Instant yearStart,
            Instant yearEnd);
}
