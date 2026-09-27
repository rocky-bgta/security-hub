package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.DurationRange;
import com.aspire.asat.cms.dto.topic.ProductPackagePairDto;
import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.util.TopicPrivacyCriteria;
import com.aspire.asat.cms.util.TopicSoftMatchDimensions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicFilterRepositoryImpl implements TopicFilterRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    private final SubPackageRepository subPackageRepository;

    @Override
    public Page<Topic> findByFilters(String packageId, TopicFilterRequest filterRequest) {
        log.info("Filtering topics for package: {} with request: {}", packageId, filterRequest);

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Mandatory package filter
        if (packageId == null || packageId.trim().isEmpty()) {
            log.error("PackageId is mandatory for topic filtering");
            throw new IllegalArgumentException("PackageId is mandatory for topic filtering");
        }
        criteriaList.add(Criteria.where("productPackageMappings.packageIds").in(packageId));
        log.info("Applied mandatory package filter: {}", packageId);

        appendSharedTopicFilters(criteriaList, filterRequest);

        List<String> selectedTopicIds = normalizeIds(filterRequest.getSelectedTopicId());
        if (!selectedTopicIds.isEmpty()) {
            // Exclude selected from paginated results so they are not duplicated
            criteriaList.add(Criteria.where("_id").nin(selectedTopicIds));
            log.info("Pinning {} selected topic(s) at the top of the result list", selectedTopicIds.size());
        }

        // Apply all criteria
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    criteriaList.toArray(new Criteria[0])
            ));
        }

        if (selectedTopicIds.isEmpty()) {
            return executeFilterQuery(query, filterRequest);
        }

        return executeFilterQueryWithSelectedFirst(packageId, query, filterRequest, selectedTopicIds);
    }

    @Override
    public Page<Topic> findByFiltersByProduct(String productId, TopicFilterRequest filterRequest) {
        log.info("Filtering topics for product: {} with request: {}", productId, filterRequest);

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Mandatory product filter
        if (productId == null || productId.trim().isEmpty()) {
            log.error("ProductId is mandatory for topic filtering");
            throw new IllegalArgumentException("ProductId is mandatory for topic filtering");
        }
        criteriaList.add(Criteria.where("productPackageMappings.productId").is(productId));
        log.info("Applied mandatory product filter: {}", productId);

        appendSharedTopicFilters(criteriaList, filterRequest);

        // Apply all criteria
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    criteriaList.toArray(new Criteria[0])
            ));
        }

        return executeFilterQuery(query, filterRequest);
    }

    @Override
    public Page<Topic> findByFiltersUnscoped(TopicFilterRequest filterRequest) {
        log.info("Filtering topics without product/package scope: {}", filterRequest);

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        appendSharedTopicFilters(criteriaList, filterRequest);

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return executeFilterQuery(query, filterRequest);
    }

    @Override
    public Page<RecommendedTopic> recommendByProduct(String productId, TopicFilterRequest filterRequest) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("ProductId is mandatory for topic recommendation");
        }
        List<Criteria> hard = new ArrayList<>();
        hard.add(Criteria.where("productPackageMappings.productId").is(productId.trim()));
        appendRecommendHardFilters(hard, filterRequest);
        return executeRecommendQuery(hard, filterRequest);
    }

    @Override
    public Page<RecommendedTopic> recommendByPackage(String packageId, TopicFilterRequest filterRequest) {
        if (packageId == null || packageId.trim().isEmpty()) {
            throw new IllegalArgumentException("PackageId is mandatory for topic recommendation");
        }
        List<Criteria> hard = new ArrayList<>();
        hard.add(Criteria.where("productPackageMappings.packageIds").in(packageId.trim()));
        appendRecommendHardFilters(hard, filterRequest);
        return executeRecommendQuery(hard, filterRequest);
    }

    @Override
    public Page<RecommendedTopic> recommendUnscoped(TopicFilterRequest filterRequest) {
        List<Criteria> hard = new ArrayList<>();
        appendRecommendHardFilters(hard, filterRequest);
        return executeRecommendQuery(hard, filterRequest);
    }

    /**
     * Hard constraints only: status + privacy. Soft metadata is scored, never AND-filtered.
     */
    private void appendRecommendHardFilters(List<Criteria> criteriaList, TopicFilterRequest filterRequest) {
        TopicFilterRequest request = filterRequest != null ? filterRequest : new TopicFilterRequest();
        if (request.getStatus() != null) {
            criteriaList.add(Criteria.where("status").is(request.getStatus()));
        } else {
            criteriaList.add(Criteria.where("status").is(TopicStatus.ENABLED));
        }
        TopicPrivacyCriteria.appendIfViewerPresent(criteriaList, request.getClientId());
    }

    private Page<RecommendedTopic> executeRecommendQuery(List<Criteria> hardCriteria, TopicFilterRequest filterRequest) {
        TopicFilterRequest request = filterRequest != null ? filterRequest : new TopicFilterRequest();
        int page = (request.getPage() != null && request.getPage() > 0) ? request.getPage() - 1 : 0;
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 20;
        long skip = (long) page * size;

        Criteria matchCriteria = hardCriteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(hardCriteria.toArray(new Criteria[0]));

        AggregationOperation addMatchScore = context -> new Document("$addFields",
                new Document("matchScore",
                        TopicSoftMatchDimensions.matchScoreExpression(request).toDocument(context)));

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(matchCriteria),
                addMatchScore,
                Aggregation.sort(Sort.by(Sort.Order.desc("matchScore"), Sort.Order.asc("topicName"))),
                Aggregation.facet(
                                Aggregation.count().as("total"))
                        .as("metadata")
                        .and(Aggregation.skip(skip), Aggregation.limit(size))
                        .as("data")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Topic.class, Document.class);
        Document root = results.getUniqueMappedResult();
        if (root == null) {
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(List.of(), pageable, 0);
        }

        long total = 0L;
        List<Document> metadata = root.getList("metadata", Document.class);
        if (metadata != null && !metadata.isEmpty()) {
            Object totalVal = metadata.get(0).get("total");
            if (totalVal instanceof Number number) {
                total = number.longValue();
            }
        }

        List<Document> data = root.getList("data", Document.class);
        List<RecommendedTopic> content = new ArrayList<>();
        if (data != null) {
            for (Document doc : data) {
                int score = 0;
                Object scoreVal = doc.get("matchScore");
                if (scoreVal instanceof Number number) {
                    score = number.intValue();
                }
                Document topicDoc = new Document(doc);
                topicDoc.remove("matchScore");
                Topic topic = mongoTemplate.getConverter().read(Topic.class, topicDoc);
                content.add(new RecommendedTopic(topic, score));
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        log.info("Recommend query returned {} of {} topics (page={}, size={})",
                content.size(), total, page, size);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public ProductPackageTopicsPage findByProductPackagePairsWithTotals(
            List<ProductPackagePairDto> productPackages,
            TopicFilterRequest filterRequest,
            boolean excludeMatching) {
        log.info("Filtering topics for {} product/package pairs (excludeMatching={}) with both totals, request: {}",
                productPackages != null ? productPackages.size() : 0, excludeMatching, filterRequest);

        TopicFilterRequest effectiveFilter = filterRequest != null ? filterRequest : new TopicFilterRequest();

        List<Criteria> pairCriteria = productPackages == null ? List.of() : productPackages.stream()
                .filter(pair -> pair.getProductId() != null && !pair.getProductId().isBlank()
                        && pair.getPackageId() != null && !pair.getPackageId().isBlank())
                .map(pair -> Criteria.where("productPackageMappings").elemMatch(
                        Criteria.where("productId").is(pair.getProductId().trim())
                                .and("packageIds").in(pair.getPackageId().trim())
                ))
                .toList();

        // No product/package scope: nothing is assigned; every topic is locked
        if (pairCriteria.isEmpty()) {
            Page<Topic> lockedPage = findByFiltersUnscoped(effectiveFilter);
            long lockedTotal = lockedPage.getTotalElements();
            Page<Topic> page = excludeMatching
                    ? lockedPage
                    : new PageImpl<>(List.of(), lockedPage.getPageable(), 0);
            log.info("No product/package pairs — assigned=0, locked={} (total topics)", lockedTotal);
            return new ProductPackageTopicsPage(page, 0L, lockedTotal);
        }

        Query assignedQuery = buildProductPackageScopeQuery(pairCriteria, effectiveFilter, false);
        Query lockedQuery = buildProductPackageScopeQuery(pairCriteria, effectiveFilter, true);

        long assignedTotal = mongoTemplate.count(assignedQuery, Topic.class);
        long lockedTotal = mongoTemplate.count(lockedQuery, Topic.class);

        List<String> selectedTopicIds = normalizeIds(effectiveFilter.getSelectedTopicId());
        Page<Topic> page;
        if (!excludeMatching && !selectedTopicIds.isEmpty()) {
            Query pagedOthersQuery = buildProductPackageScopeQuery(pairCriteria, effectiveFilter, false);
            page = executeFilterQueryWithSelectedFirstForPairs(
                    pairCriteria, pagedOthersQuery, effectiveFilter, selectedTopicIds);
            // selected-first path recomputes its own total; keep assignedTotal from count above
        } else {
            Query pageQuery = excludeMatching ? lockedQuery : assignedQuery;
            page = executeFilterQuery(pageQuery, effectiveFilter);
        }

        log.info("Product/package topic totals: assigned={}, locked={}, returning {} items (excludeMatching={})",
                assignedTotal, lockedTotal, page.getNumberOfElements(), excludeMatching);

        return new ProductPackageTopicsPage(page, assignedTotal, lockedTotal);
    }

    private Query buildProductPackageScopeQuery(
            List<Criteria> pairCriteria,
            TopicFilterRequest effectiveFilter,
            boolean excludeMatching) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (excludeMatching) {
            criteriaList.add(new Criteria().norOperator(pairCriteria.toArray(new Criteria[0])));
        } else {
            criteriaList.add(new Criteria().orOperator(pairCriteria.toArray(new Criteria[0])));
        }
        appendSharedTopicFilters(criteriaList, effectiveFilter);

        List<String> selectedTopicIds = normalizeIds(effectiveFilter.getSelectedTopicId());
        if (!selectedTopicIds.isEmpty() && !excludeMatching) {
            criteriaList.add(Criteria.where("_id").nin(selectedTopicIds));
        }

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        return query;
    }

    private Page<Topic> executeFilterQueryWithSelectedFirstForPairs(
            List<Criteria> pairCriteria,
            Query othersQuery,
            TopicFilterRequest filterRequest,
            List<String> selectedTopicIds) {

        List<Criteria> selectedCriteria = new ArrayList<>();
        selectedCriteria.add(new Criteria().orOperator(pairCriteria.toArray(new Criteria[0])));
        selectedCriteria.add(Criteria.where("_id").in(selectedTopicIds));
        if (filterRequest.getStatus() != null) {
            selectedCriteria.add(Criteria.where("status").is(filterRequest.getStatus()));
        } else {
            selectedCriteria.add(Criteria.where("status").in(TopicStatus.ENABLED, TopicStatus.DISABLED));
        }

        Query selectedQuery = new Query(new Criteria().andOperator(selectedCriteria.toArray(new Criteria[0])));
        List<Topic> selectedTopics = orderTopicsByIds(
                mongoTemplate.find(selectedQuery, Topic.class),
                selectedTopicIds);
        log.info("Selected topics found for product/package pairs: {}", selectedTopics.size());

        Page<Topic> othersPage = executeFilterQuery(othersQuery, filterRequest);

        List<Topic> merged = new ArrayList<>(selectedTopics.size() + othersPage.getContent().size());
        merged.addAll(selectedTopics);
        merged.addAll(othersPage.getContent());

        long totalElements = othersPage.getTotalElements() + selectedTopics.size();
        return new PageImpl<>(merged, othersPage.getPageable(), totalElements);
    }

    /**
     * Shared filter block used by both package- and product-scoped queries. The caller is
     * expected to have already appended the mandatory scope criterion. Each block is null-/
     * empty-guarded so it only contributes when the request supplies a value.
     */
    private void appendSharedTopicFilters(List<Criteria> criteriaList, TopicFilterRequest filterRequest) {
        // Status filter - only apply if status is provided, otherwise default to ENABLED+DISABLED
        if (filterRequest.getStatus() != null) {
            criteriaList.add(Criteria.where("status").is(filterRequest.getStatus()));
        } else {
            criteriaList.add(Criteria.where("status").in(TopicStatus.ENABLED, TopicStatus.DISABLED));
        }

        if (StringUtils.hasText(filterRequest.getSearchText())) {
            String searchPattern = ".*" + filterRequest.getSearchText() + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("topicName").regex(searchPattern, "i"),
                    Criteria.where("description").regex(searchPattern, "i")
            ));
        }

        if (filterRequest.getCountryIds() != null && !filterRequest.getCountryIds().isEmpty()) {
            criteriaList.add(Criteria.where("countryIds").in(filterRequest.getCountryIds()));
        }

        if (filterRequest.getComplianceIds() != null && !filterRequest.getComplianceIds().isEmpty()) {
            criteriaList.add(Criteria.where("complianceIds").in(filterRequest.getComplianceIds()));
        }

        if (filterRequest.getCategoryIds() != null && !filterRequest.getCategoryIds().isEmpty()) {
            criteriaList.add(Criteria.where("categoryIds").in(filterRequest.getCategoryIds()));
        }

        if (filterRequest.getContentTypeId() != null && !filterRequest.getContentTypeId().isEmpty()) {
            criteriaList.add(Criteria.where("contentTypeId").in(filterRequest.getContentTypeId()));
        }

        if (filterRequest.getDurationRanges() != null && !filterRequest.getDurationRanges().isEmpty()) {
            List<Criteria> durationCriteriaList = new ArrayList<>();
            for (DurationRange range : filterRequest.getDurationRanges()) {
                durationCriteriaList.add(Criteria.where("durationMinutes")
                        .gte(range.getMinMinutes())
                        .lte(range.getMaxMinutes()));
            }
            criteriaList.add(new Criteria().orOperator(durationCriteriaList.toArray(new Criteria[0])));
        }

        // Multi-value id filters against embedded {<type>Id, <type>Name} arrays (OR within dimension).
        addElemMatchIds(criteriaList, "payloadType",               "payloadTypeId",               filterRequest.getPayloadTypeIds());
        addElemMatchIds(criteriaList, "difficulty",                "difficultyId",                filterRequest.getDifficultyIds());
        addElemMatchIds(criteriaList, "tone",                      "toneId",                      filterRequest.getToneIds());
        addElemMatchIds(criteriaList, "attackerPersona",           "attackerPersonaId",           filterRequest.getAttackerPersonaIds());
        addElemMatchIds(criteriaList, "socialEngineeringStrategy", "socialEngineeringStrategyId", filterRequest.getSocialEngineeringStrategyIds());
        addElemMatchIds(criteriaList, "campaignObjective",         "campaignObjectiveId",         filterRequest.getCampaignObjectiveIds());
        addElemMatchIds(criteriaList, "triggerEvent",              "triggerEventId",              filterRequest.getTriggerEventIds());
        addElemMatchIds(criteriaList, "attackTechnique",           "attackTechniqueId",           filterRequest.getAttackTechniqueIds());
        addElemMatchIds(criteriaList, "emotionalTrigger",          "emotionalTriggerId",          filterRequest.getEmotionalTriggerIds());
        addElemMatchIds(criteriaList, "urgencyLevel",              "urgencyLevelId",              filterRequest.getUrgencyLevelIds());
        addElemMatchIds(criteriaList, "brand",                     "brandId",                     filterRequest.getBrandIds());
        addElemMatchIds(criteriaList, "callToAction",              "callToActionId",              filterRequest.getCallToActionIds());
        addElemMatchIds(criteriaList, "industry",                  "industryId",                  filterRequest.getIndustryIds());
        addElemMatchIds(criteriaList, "subIndustry",               "subIndustryId",               filterRequest.getSubIndustryIds());

        // tags is a primitive multikey array; match topics with ANY of the supplied tags.
        if (filterRequest.getTags() != null && !filterRequest.getTags().isEmpty()) {
            criteriaList.add(Criteria.where("tags").in(filterRequest.getTags()));
        }

        TopicPrivacyCriteria.appendIfViewerPresent(criteriaList, filterRequest.getClientId());
    }

    private static void addElemMatchIds(List<Criteria> criteriaList, String arrayField, String idField, List<String> ids) {
        List<String> normalized = normalizeIds(ids);
        if (!normalized.isEmpty()) {
            criteriaList.add(Criteria.where(arrayField)
                    .elemMatch(Criteria.where(idField).in(normalized)));
        }
    }

    private static List<String> normalizeIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    /**
     * Resolves pagination + sort from the filter request, counts, applies the page window, and
     * returns the paged result. Centralized so package- and product-scoped queries stay in sync.
     */
    private Page<Topic> executeFilterQuery(Query query, TopicFilterRequest filterRequest) {
        // Handle pagination - convert 1-based page to 0-based page index
        int page = (filterRequest.getPage() != null && filterRequest.getPage() > 0) ? filterRequest.getPage() - 1 : 0;
        int size = (filterRequest.getSize() != null && filterRequest.getSize() > 0) ? filterRequest.getSize() : 20;

        // Handle sorting
        Sort.Direction direction = Sort.Direction.ASC;
        if (filterRequest.getSortDirection() != null) {
            try {
                direction = Sort.Direction.fromString(filterRequest.getSortDirection());
            } catch (IllegalArgumentException e) {
                // Default to ASC if invalid direction provided
                direction = Sort.Direction.ASC;
            }
        }

        String sortBy = (filterRequest.getSortBy() != null && !filterRequest.getSortBy().trim().isEmpty())
                ? filterRequest.getSortBy() : "topicName";

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        long total = mongoTemplate.count(query, Topic.class);
        log.info("Total topics found: {}", total);

        query.with(pageable);
        List<Topic> topics = mongoTemplate.find(query, Topic.class);
        log.info("Topics returned after pagination: {}", topics.size());

        return new PageImpl<>(topics, pageable, total);
    }

    /**
     * Fetches selected topics for the package first (preserving request order), then appends
     * the paginated remaining topics (already excluding selected IDs in {@code othersQuery}).
     */
    private Page<Topic> executeFilterQueryWithSelectedFirst(
            String packageId,
            Query othersQuery,
            TopicFilterRequest filterRequest,
            List<String> selectedTopicIds) {

        List<Criteria> selectedCriteria = new ArrayList<>();
        selectedCriteria.add(Criteria.where("productPackageMappings.packageIds").in(packageId));
        selectedCriteria.add(Criteria.where("_id").in(selectedTopicIds));
        if (filterRequest.getStatus() != null) {
            selectedCriteria.add(Criteria.where("status").is(filterRequest.getStatus()));
        } else {
            selectedCriteria.add(Criteria.where("status").in(TopicStatus.ENABLED, TopicStatus.DISABLED));
        }
        TopicPrivacyCriteria.appendIfViewerPresent(selectedCriteria, filterRequest.getClientId());

        Query selectedQuery = new Query(new Criteria().andOperator(selectedCriteria.toArray(new Criteria[0])));
        List<Topic> selectedTopics = orderTopicsByIds(
                mongoTemplate.find(selectedQuery, Topic.class),
                selectedTopicIds);
        log.info("Selected topics found for package {}: {}", packageId, selectedTopics.size());

        Page<Topic> othersPage = executeFilterQuery(othersQuery, filterRequest);

        List<Topic> merged = new ArrayList<>(selectedTopics.size() + othersPage.getContent().size());
        merged.addAll(selectedTopics);
        merged.addAll(othersPage.getContent());

        long totalElements = othersPage.getTotalElements() + selectedTopics.size();
        log.info("Returning {} selected + {} other topics (totalElements={})",
                selectedTopics.size(), othersPage.getContent().size(), totalElements);

        return new PageImpl<>(merged, othersPage.getPageable(), totalElements);
    }

    private static List<Topic> orderTopicsByIds(List<Topic> topics, List<String> orderedIds) {
        if (topics == null || topics.isEmpty() || orderedIds == null || orderedIds.isEmpty()) {
            return topics == null ? List.of() : topics;
        }
        Map<String, Topic> byId = new HashMap<>();
        for (Topic topic : topics) {
            if (topic.getId() != null) {
                byId.put(topic.getId(), topic);
            }
        }
        List<Topic> ordered = new ArrayList<>();
        for (String id : orderedIds) {
            Topic topic = byId.get(id);
            if (topic != null) {
                ordered.add(topic);
            }
        }
        return ordered;
    }

    @Override
    public Long countTopicsBySubPackage(String subPackageId) {
        if (subPackageId == null || subPackageId.trim().isEmpty()) {
            log.error("SubPackageId is mandatory for counting topics");
            throw new IllegalArgumentException("SubPackageId is mandatory for counting topics");
        }
        
        // Get the sub-package to find topic IDs
        SubPackage subPackage = subPackageRepository.findById(subPackageId)
                .orElseThrow(() -> new IllegalArgumentException("Sub-package not found: " + subPackageId));
        
        List<String> topicIds = subPackage.getTopicId();
        if (topicIds == null || topicIds.isEmpty()) {
            log.info("No topics found for sub-package: {}", subPackageId);
            return 0L;
        }
        
        // Count active topics by their IDs
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").in(topicIds)
                .and("status").is(TopicStatus.ENABLED));
        long count = mongoTemplate.count(query, Topic.class);
        log.info("Total enabled topics count for sub-package {}: {}", subPackageId, count);
        return count;
    }

    @Override
    public Long countUniqueTopicsByProductAndPackage(List<String> productIds, List<String> packageIds) {
        log.info("Counting unique topics for productIds: {} and packageIds: {}", productIds, packageIds);

        if ((productIds == null || productIds.isEmpty()) && (packageIds == null || packageIds.isEmpty())) {
            log.info("Both productIds and packageIds are empty, returning 0");
            return 0L;
        }

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Build criteria for matching topics
        // Topic matches if productPackageMappings array has at least one element where:
        // - productId is in productIds list AND
        // - packageIds array contains any of the packageIds
        
        List<Criteria> orCriteriaList = new ArrayList<>();

        if (productIds != null && !productIds.isEmpty() && packageIds != null && !packageIds.isEmpty()) {
            // Both productIds and packageIds provided
            // Match topics where at least one productPackageMapping has:
            // - productId in productIds AND packageIds array contains any packageId
            for (String productId : productIds) {
                // Match topics where productPackageMappings array contains:
                // - an element with productId matching AND packageIds array contains any packageId
                Criteria productCriteria = new Criteria().andOperator(
                    Criteria.where("productPackageMappings").elemMatch(
                        Criteria.where("productId").is(productId)
                                .and("packageIds").in(packageIds)
                    )
                );
                orCriteriaList.add(productCriteria);
            }
        } else if (productIds != null && !productIds.isEmpty()) {
            // Only productIds provided - match any topic where productPackageMappings contains these productIds
            orCriteriaList.add(
                Criteria.where("productPackageMappings").elemMatch(
                    Criteria.where("productId").in(productIds)
                )
            );
        } else if (packageIds != null && !packageIds.isEmpty()) {
            // Only packageIds provided - match any topic where packageIds array contains any of these
            orCriteriaList.add(
                Criteria.where("productPackageMappings").elemMatch(
                    Criteria.where("packageIds").in(packageIds)
                )
            );
        }

        if (!orCriteriaList.isEmpty()) {
            if (orCriteriaList.size() == 1) {
                criteriaList.add(orCriteriaList.get(0));
            } else {
                criteriaList.add(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
            }
        }

        // Only consider enabled topics
        criteriaList.add(Criteria.where("status").is(TopicStatus.ENABLED));

        // Combine all criteria with AND
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // Execute query and get topics
        List<Topic> topics = mongoTemplate.find(query, Topic.class);
        
        // Ensure uniqueness by topic ID (in case a topic matches multiple criteria)
        Set<String> uniqueTopicIds = new HashSet<>();
        for (Topic topic : topics) {
            if (topic.getId() != null) {
                uniqueTopicIds.add(topic.getId());
            }
        }
        
        long uniqueCount = uniqueTopicIds.size();
        log.info("Found {} topics, {} unique topics for productIds: {} and packageIds: {}", 
                topics.size(), uniqueCount, productIds, packageIds);

        return uniqueCount;
    }

    @Override
    public long countTopicsByProductPackagePairs(List<ProductPackagePairDto> productPackages) {
        List<Criteria> pairCriteria = productPackages == null ? List.of() : productPackages.stream()
                .filter(pair -> pair.getProductId() != null && !pair.getProductId().isBlank()
                        && pair.getPackageId() != null && !pair.getPackageId().isBlank())
                .map(pair -> Criteria.where("productPackageMappings").elemMatch(
                        Criteria.where("productId").is(pair.getProductId().trim())
                                .and("packageIds").in(pair.getPackageId().trim())
                ))
                .toList();

        if (pairCriteria.isEmpty()) {
            return 0L;
        }

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                new Criteria().orOperator(pairCriteria.toArray(new Criteria[0])),
                Criteria.where("status").is(TopicStatus.ENABLED)
        ));

        long count = mongoTemplate.count(query, Topic.class);
        log.info("Counted {} ENABLED topics for {} product/package pairs", count, pairCriteria.size());
        return count;
    }

    @Override
    public Map<Integer, Long> countTopicsByProductPackagePairsByMonth(
            List<ProductPackagePairDto> productPackages,
            Instant yearStart,
            Instant yearEnd) {

        List<Criteria> pairCriteria = productPackages == null ? List.of() : productPackages.stream()
                .filter(pair -> pair.getProductId() != null && !pair.getProductId().isBlank()
                        && pair.getPackageId() != null && !pair.getPackageId().isBlank())
                .map(pair -> Criteria.where("productPackageMappings").elemMatch(
                        Criteria.where("productId").is(pair.getProductId().trim())
                                .and("packageIds").in(pair.getPackageId().trim())
                ))
                .toList();

        if (pairCriteria.isEmpty() || yearStart == null || yearEnd == null) {
            return Map.of();
        }

        MatchOperation match = Aggregation.match(new Criteria().andOperator(
                new Criteria().orOperator(pairCriteria.toArray(new Criteria[0])),
                Criteria.where("status").is(TopicStatus.ENABLED),
                Criteria.where("createdAt").gte(yearStart).lte(yearEnd)
        ));

        ProjectionOperation project = Aggregation.project()
                .andExpression("month(createdAt)").as("month")
                .and("_id").as("topicId");

        GroupOperation group = Aggregation.group("month").count().as("count");

        Aggregation aggregation = Aggregation.newAggregation(match, project, group);
        List<org.bson.Document> results = mongoTemplate.aggregate(aggregation, "topic", org.bson.Document.class)
                .getMappedResults();

        Map<Integer, Long> monthCounts = new HashMap<>();
        for (org.bson.Document doc : results) {
            Integer month = doc.getInteger("_id");
            Number countNumber = (Number) doc.get("count");
            Long count = countNumber != null ? countNumber.longValue() : null;
            if (month != null && month >= 1 && month <= 12 && count != null) {
                monthCounts.put(month, count);
            }
        }

        log.info("Monthly topic counts for {} pairs across {} months with data",
                pairCriteria.size(), monthCounts.size());
        return monthCounts;
    }
}
