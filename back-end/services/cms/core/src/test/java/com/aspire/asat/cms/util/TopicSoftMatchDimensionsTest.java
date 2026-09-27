package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TopicSoftMatchDimensionsTest {

    @Test
    void matchScoreExpression_withNoSoftCriteria_returnsLiteralZero() {
        Document doc = TopicSoftMatchDimensions.matchScoreExpression(new TopicFilterRequest())
                .toDocument(mock(AggregationOperationContext.class));
        assertEquals(0, doc.get("$literal"));
    }

    @Test
    void matchScoreExpression_sumsPresentDimensionsIncludingIndustryAndTags() {
        TopicFilterRequest request = TopicFilterRequest.builder()
                .payloadTypeIds(List.of("payload-1"))
                .difficultyIds(List.of("diff-1"))
                .industryIds(List.of("industry-missing"))
                .tags(List.of("Security", "Phishing"))
                .build();

        Document doc = TopicSoftMatchDimensions.matchScoreExpression(request)
                .toDocument(mock(AggregationOperationContext.class));

        assertTrue(doc.containsKey("$add"));
        @SuppressWarnings("unchecked")
        List<Object> terms = (List<Object>) doc.get("$add");
        assertEquals(4, terms.size());
        for (Object term : terms) {
            assertInstanceOf(Document.class, term);
            assertTrue(((Document) term).containsKey("$cond"));
        }
    }

    @Test
    void normalizeIds_trimsAndDedupes() {
        assertEquals(List.of("a", "b"),
                TopicSoftMatchDimensions.normalizeIds(java.util.Arrays.asList(" a ", "b", "a", "", null)));
    }
}
