package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Soft-match dimensions for topic recommendation scoring.
 * Each present request dimension contributes +1 when the topic matches (OR within dimension).
 * Unmatched dimensions contribute 0 and never hard-fail the query.
 */
public final class TopicSoftMatchDimensions {

    public record EmbeddedIdDimension(
            String arrayField,
            String idField,
            Function<TopicFilterRequest, List<String>> idsFromRequest) {
    }

    public record ScalarListDimension(
            String arrayField,
            Function<TopicFilterRequest, List<String>> idsFromRequest) {
    }

    private static final List<EmbeddedIdDimension> EMBEDDED_ID_DIMENSIONS = List.of(
            new EmbeddedIdDimension("payloadType", "payloadTypeId", TopicFilterRequest::getPayloadTypeIds),
            new EmbeddedIdDimension("difficulty", "difficultyId", TopicFilterRequest::getDifficultyIds),
            new EmbeddedIdDimension("tone", "toneId", TopicFilterRequest::getToneIds),
            new EmbeddedIdDimension("attackerPersona", "attackerPersonaId", TopicFilterRequest::getAttackerPersonaIds),
            new EmbeddedIdDimension("socialEngineeringStrategy", "socialEngineeringStrategyId",
                    TopicFilterRequest::getSocialEngineeringStrategyIds),
            new EmbeddedIdDimension("campaignObjective", "campaignObjectiveId", TopicFilterRequest::getCampaignObjectiveIds),
            new EmbeddedIdDimension("triggerEvent", "triggerEventId", TopicFilterRequest::getTriggerEventIds),
            new EmbeddedIdDimension("attackTechnique", "attackTechniqueId", TopicFilterRequest::getAttackTechniqueIds),
            new EmbeddedIdDimension("emotionalTrigger", "emotionalTriggerId", TopicFilterRequest::getEmotionalTriggerIds),
            new EmbeddedIdDimension("urgencyLevel", "urgencyLevelId", TopicFilterRequest::getUrgencyLevelIds),
            new EmbeddedIdDimension("brand", "brandId", TopicFilterRequest::getBrandIds),
            new EmbeddedIdDimension("callToAction", "callToActionId", TopicFilterRequest::getCallToActionIds),
            new EmbeddedIdDimension("industry", "industryId", TopicFilterRequest::getIndustryIds),
            new EmbeddedIdDimension("subIndustry", "subIndustryId", TopicFilterRequest::getSubIndustryIds)
    );

    private static final List<ScalarListDimension> SCALAR_LIST_DIMENSIONS = List.of(
            new ScalarListDimension("countryIds", TopicFilterRequest::getCountryIds),
            new ScalarListDimension("complianceIds", TopicFilterRequest::getComplianceIds),
            new ScalarListDimension("tags", TopicFilterRequest::getTags)
    );

    private TopicSoftMatchDimensions() {
    }

    public static List<String> normalizeIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    /**
     * Builds a Mongo aggregation expression that sums +1 for each soft dimension present
     * on the request that intersects the topic document.
     */
    public static AggregationExpression matchScoreExpression(TopicFilterRequest request) {
        List<Document> terms = new ArrayList<>();

        for (EmbeddedIdDimension dim : EMBEDDED_ID_DIMENSIONS) {
            List<String> ids = normalizeIds(dim.idsFromRequest().apply(request));
            if (ids.isEmpty()) {
                continue;
            }
            terms.add(embeddedIdMatchTerm(dim.arrayField(), dim.idField(), ids));
        }

        for (ScalarListDimension dim : SCALAR_LIST_DIMENSIONS) {
            List<String> ids = normalizeIds(dim.idsFromRequest().apply(request));
            if (ids.isEmpty()) {
                continue;
            }
            terms.add(scalarListMatchTerm(dim.arrayField(), ids));
        }

        if (terms.isEmpty()) {
            return context -> new Document("$literal", 0);
        }

        Document sum = new Document("$add", terms);
        return context -> sum;
    }

    private static Document embeddedIdMatchTerm(String arrayField, String idField, List<String> requestIds) {
        Document mappedIds = new Document("$map", new Document()
                .append("input", new Document("$ifNull", List.of("$" + arrayField, List.of())))
                .append("as", "item")
                .append("in", "$$item." + idField));

        Document intersection = new Document("$setIntersection", List.of(mappedIds, requestIds));
        Document size = new Document("$size", new Document("$ifNull", List.of(intersection, List.of())));

        return new Document("$cond", new Document()
                .append("if", new Document("$gt", List.of(size, 0)))
                .append("then", 1)
                .append("else", 0));
    }

    private static Document scalarListMatchTerm(String arrayField, List<String> requestIds) {
        Document intersection = new Document("$setIntersection", List.of(
                new Document("$ifNull", List.of("$" + arrayField, List.of())),
                requestIds));
        Document size = new Document("$size", new Document("$ifNull", List.of(intersection, List.of())));

        return new Document("$cond", new Document()
                .append("if", new Document("$gt", List.of(size, 0)))
                .append("then", 1)
                .append("else", 0));
    }
}
