package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.topic.ProductPackagePairDto;
import com.aspire.asat.cms.dto.topic.TopicMinimalDto;
import com.aspire.asat.cms.model.topic.Category;
import com.aspire.asat.cms.model.topic.ContentType;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.util.TopicPrivacyCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicRepositoryCustomImpl implements TopicRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    private final ContentTypeRepository contentTypeRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Document getFullTopicHierarchy(String userId, String topicId, String subpackageId) {
        log.info("Fetching full topic hierarchy for userId={}, topicId={}, subpackageId={}", userId, topicId, subpackageId);

        // Step 1: Match user topic enrollment
        MatchOperation match = Aggregation.match(
                Criteria.where("userId").is(userId)
                        .and("topicId").is(topicId)
                        .and("subPackageId").is(subpackageId)
        );

        // Step 2: Join with topic collection
        LookupOperation joinTopic = Aggregation.lookup("topic", "topicId", "_id", "topic");
        UnwindOperation unwindTopic = Aggregation.unwind("topic");

        // Step 3: Extract chapter IDs from topic
        AddFieldsOperation addChapterIds = AddFieldsOperation.builder()
                .addFieldWithValue("chapterIds", "$topic.chapterIds")
                .build();

        // Step 4: Join with chapters collection
        LookupOperation joinChapters = Aggregation.lookup("chapter", "chapterIds", "_id", "chapters");

        // Step 5: Flatten all chapter contentIds into a single array
        AddFieldsOperation addContentIds = AddFieldsOperation.builder()
                .addFieldWithValue("contentIds",
                        new Document("$reduce",
                                new Document("input", "$chapters.contentIds")
                                        .append("initialValue", Collections.emptyList())
                                        .append("in", new Document("$concatArrays", List.of("$$value", "$$this")))))
                .build();

        // Step 6: Join with contents collection
        LookupOperation joinContents = Aggregation.lookup("content", "contentIds", "_id", "contents");

        // Step 7: Join with user content status
        LookupOperation joinContentStatus = Aggregation.lookup(
                "user_content_status", "contentIds", "contentId", "contentStatus"
        );

        // Step 8: Filter content status to only this user's records
        AddFieldsOperation keepOnlyThisUsersStatus = AddFieldsOperation.builder()
                .addFieldWithValue("contentStatus",
                        new Document("$filter", new Document()
                                .append("input", "$contentStatus")
                                .append("as", "cs")
                                .append("cond", new Document("$and", List.of(
                                        new Document("$eq", List.of("$$cs.userId", userId)),
                                        new Document("$eq", List.of("$$cs.topicId", topicId)),
                                        new Document("$eq", List.of("$$cs.subPackageId", subpackageId)),
                                        new Document("$in", List.of("$$cs.contentId", "$contentIds"))
                                )))
                        )
                ).build();

        // Step 9: Get assigned content IDs
        AddFieldsOperation addAssignedContentIds = AddFieldsOperation.builder()
                .addFieldWithValue("assignedContentIds",
                        new Document("$map", new Document()
                                .append("input", "$contentStatus")
                                .append("as", "cs")
                                .append("in", "$$cs.contentId")
                        )
                ).build();

        // Step 10: Filter contents to only assigned content
        AddFieldsOperation filterContentsToAssigned = AddFieldsOperation.builder()
                .addFieldWithValue("contents",
                        new Document("$filter", new Document()
                                .append("input", "$contents")
                                .append("as", "c")
                                .append("cond", new Document("$in", List.of("$$c._id", "$assignedContentIds")))
                        )
                ).build();

        // Step 11: Project final fields
        // Note: Since we're starting from user_topics collection,
        // all fields (status, isSaved, progress) are directly available
        ProjectionOperation project = Aggregation.project()
                .and("topicId").as("topicId")
                .and("subPackageId").as("subPackageId")
                .and("status").as("status")                    // from user_topics
                .and("isSaved").as("isSaved")                  // from user_topics
                .and("progress").as("progress")                // from user_topics
                .and("topic.topicName").as("topicName")
                .and("topic.description").as("topicDescription")
                .and("topic.thumbnailUrl").as("thumbnailUrl")
                .and("topic.durationMinutes").as("durationMinutes")
                .and("topic.categoryIds").as("categoryIds")
                .and("topic.countryIds").as("countryIds")
                .and("topic.complianceIds").as("complianceIds")
                .and("topic.contentTypeId").as("contentTypeId")
                .and("topic.createdAt").as("publishDate")
                .and("chapters").as("chapters")
                .and("contents").as("contents")          // now filtered to assigned only
                .and("contentStatus").as("contentStatus"); // filtered to this user/topic/subpackage

        // Execute aggregation pipeline
        // Note: Starting from user_topics collection
        // All user topic fields are now directly available, no need to join with user_topics
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                joinTopic, unwindTopic,
                addChapterIds, joinChapters,
                addContentIds, joinContents,
                joinContentStatus, keepOnlyThisUsersStatus,
                addAssignedContentIds, filterContentsToAssigned,
                project
        );

        Document result = mongoTemplate.aggregate(aggregation, "user_topics", Document.class).getUniqueMappedResult();
        log.info("Topic hierarchy fetched successfully for userId={}, topicId={}", userId, topicId);
        return result;
    }

    @Override
    public Page<TopicMinimalDto> findTopicsByProductAndPackage(String productId, String packageId, String search,
                                                               Pageable pageable, int offset, int pageSize,
                                                               String viewerClientId) {
        log.info("Finding topics by productId: {}, packageId: {}, search: {}, offset: {}, pageSize: {}", productId, packageId, search, offset, pageSize);

        // Build query criteria
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Mandatory filters: productId, packageId, and ENABLED status
        criteriaList.add(Criteria.where("productPackageMappings").elemMatch(
                Criteria.where("productId").is(productId)
                        .and("packageIds").in(packageId)
        ));
        criteriaList.add(Criteria.where("status").is(TopicStatus.ENABLED));

        // Optional search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = ".*" + search.trim() + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("topicName").regex(searchPattern, "i"),
                    Criteria.where("description").regex(searchPattern, "i")
            ));
        }

        TopicPrivacyCriteria.appendIfViewerPresent(criteriaList, viewerClientId);

        // Combine all criteria with AND
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // Get total count before pagination
        long total = mongoTemplate.count(query, Topic.class);
        log.debug("Total topics found: {}", total);

        // Apply sorting from pageable (for consistent sorting)
        if (pageable.getSort().isSorted()) {
            query.with(pageable.getSort());
        }

        // Apply offset-based pagination using MongoDB skip() and limit()
        // offset parameter is already calculated as skipSize (offset * pageSize) in service layer
        query.skip(offset);
        query.limit(pageSize);
        
        log.debug("MongoDB query pagination: skip={}, limit={}", offset, pageSize);

        // Fetch topics
        List<Topic> topics = mongoTemplate.find(query, Topic.class);
        log.debug("Topics returned after pagination (offset={}, pageSize={}): {}", offset, pageSize, topics.size());

        List<TopicMinimalDto> topicMinimalDtos = mapTopicsToMinimalDtos(topics);

        int pageNumber = pageSize > 0 ? offset / pageSize : 0;
        Pageable actualPageable = PageRequest.of(pageNumber, pageSize, pageable.getSort());
        
        log.debug("Page metadata: pageNumber={}, pageSize={}, total={}", pageNumber, pageSize, total);
        
        return new PageImpl<>(topicMinimalDtos, actualPageable, total);
    }

    @Override
    public Page<TopicMinimalDto> findTopicsByProductPackagePairs(
            List<ProductPackagePairDto> productPackages,
            String search,
            Pageable pageable,
            int offset,
            int pageSize) {
        log.info("Finding topics by {} product/package pairs, search: {}, offset: {}, pageSize: {}",
                productPackages != null ? productPackages.size() : 0, search, offset, pageSize);

        if (productPackages == null || productPackages.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Criteria> pairCriteria = productPackages.stream()
                .filter(pair -> pair.getProductId() != null && !pair.getProductId().isBlank()
                        && pair.getPackageId() != null && !pair.getPackageId().isBlank())
                .map(pair -> Criteria.where("productPackageMappings").elemMatch(
                        Criteria.where("productId").is(pair.getProductId())
                                .and("packageIds").in(pair.getPackageId())
                ))
                .toList();

        if (pairCriteria.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(new Criteria().orOperator(pairCriteria.toArray(new Criteria[0])));
        criteriaList.add(Criteria.where("status").is(TopicStatus.ENABLED));

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = ".*" + search.trim() + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("topicName").regex(searchPattern, "i"),
                    Criteria.where("description").regex(searchPattern, "i")
            ));
        }

        query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        long total = mongoTemplate.count(query, Topic.class);

        if (pageable.getSort().isSorted()) {
            query.with(pageable.getSort());
        }
        query.skip(offset);
        query.limit(pageSize);

        List<Topic> topics = mongoTemplate.find(query, Topic.class);
        List<TopicMinimalDto> topicMinimalDtos = mapTopicsToMinimalDtos(topics);

        int pageNumber = pageSize > 0 ? offset / pageSize : 0;
        Pageable actualPageable = PageRequest.of(pageNumber, pageSize, pageable.getSort());
        return new PageImpl<>(topicMinimalDtos, actualPageable, total);
    }

    private List<TopicMinimalDto> mapTopicsToMinimalDtos(List<Topic> topics) {
        Set<String> contentTypeIds = topics.stream()
                .map(Topic::getContentTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> categoryIds = topics.stream()
                .map(Topic::getCategoryIds)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        Map<String, String> contentTypeMap = contentTypeRepository.findAllById(contentTypeIds)
                .stream()
                .collect(Collectors.toMap(ContentType::getId, ContentType::getTypeName));

        Map<String, String> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName));

        return topics.stream()
                .map(topic -> {
                    String contentType = topic.getContentTypeId() != null
                            ? contentTypeMap.get(topic.getContentTypeId())
                            : null;

                    List<String> categories = null;
                    if (topic.getCategoryIds() != null && !topic.getCategoryIds().isEmpty()) {
                        categories = topic.getCategoryIds().stream()
                                .map(categoryMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                    }

                    return TopicMinimalDto.builder()
                            .topicId(topic.getId())
                            .topicName(topic.getTopicName())
                            .description(topic.getDescription())
                            .durationMinutes(topic.getDurationMinutes())
                            .thumbnail(topic.getThumbnailUrl())
                            .contentType(contentType)
                            .category(categories)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
