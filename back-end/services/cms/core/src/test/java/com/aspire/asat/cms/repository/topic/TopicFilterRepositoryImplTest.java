package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.dto.topic.TopicFilterRequest;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.SubPackageRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicFilterRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private SubPackageRepository subPackageRepository;

    @InjectMocks
    private TopicFilterRepositoryImpl repository;

    @Captor
    private ArgumentCaptor<Query> queryCaptor;

    @Test
    void findByFilters_appliesMultiDifficultyIdsWithElemMatchIn() {
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Topic.class))).thenReturn(List.of());

        TopicFilterRequest request = TopicFilterRequest.builder()
                .difficultyIds(List.of("d1", "d2"))
                .page(1)
                .size(10)
                .build();

        repository.findByFilters("pkg-1", request);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Topic.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "difficulty"));
        assertTrue(documentContainsKey(queryObject, "difficultyId"));
        assertTrue(documentContainsValue(queryObject, "d1"));
        assertTrue(documentContainsValue(queryObject, "d2"));
        assertTrue(documentContainsKey(queryObject, "$in"));
        assertTrue(documentContainsKey(queryObject, "$elemMatch"));
    }

    @Test
    void findByFiltersByProduct_andAcrossPayloadAndDifficultyDimensions() {
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Topic.class))).thenReturn(List.of());

        TopicFilterRequest request = TopicFilterRequest.builder()
                .payloadTypeIds(List.of("payload-1"))
                .difficultyIds(List.of("d1"))
                .page(1)
                .size(10)
                .build();

        repository.findByFiltersByProduct("product-1", request);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Topic.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "payloadType"));
        assertTrue(documentContainsKey(queryObject, "difficulty"));
        assertTrue(documentContainsKey(queryObject, "$and"));
    }

    @Test
    void findByFilters_normalizesIdsStripsBlanksAndDuplicates() {
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Topic.class))).thenReturn(List.of());

        List<String> rawIds = new ArrayList<>();
        rawIds.add("d1");
        rawIds.add("");
        rawIds.add("  d1  ");
        rawIds.add(null);
        rawIds.add("d2");

        TopicFilterRequest request = TopicFilterRequest.builder()
                .toneIds(rawIds)
                .page(1)
                .size(10)
                .build();

        repository.findByFilters("pkg-1", request);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Topic.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsValue(queryObject, "d1"));
        assertTrue(documentContainsValue(queryObject, "d2"));
        assertFalse(documentContainsValue(queryObject, "  d1  "));
    }

    @Test
    void findByFiltersUnscoped_appliesSharedFiltersWithoutProductOrPackage() {
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Topic.class))).thenReturn(List.of());

        TopicFilterRequest request = TopicFilterRequest.builder()
                .difficultyIds(List.of("d1"))
                .page(1)
                .size(10)
                .build();

        repository.findByFiltersUnscoped(request);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Topic.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "difficulty"));
        assertFalse(documentContainsKey(queryObject, "productPackageMappings"));
    }

    @Test
    void findByFilters_omitsEmptyMetadataDimension() {
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Topic.class))).thenReturn(List.of());

        TopicFilterRequest request = TopicFilterRequest.builder()
                .difficultyIds(List.of())
                .page(1)
                .size(10)
                .build();

        repository.findByFilters("pkg-1", request);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Topic.class));
        assertFalse(documentContainsKey(queryCaptor.getValue().getQueryObject(), "difficulty"));
    }

    @Test
    void findByFilters_emptySelectedTopicId_usesExistingPaginationOnly() {
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Topic.class))).thenReturn(List.of());

        TopicFilterRequest request = TopicFilterRequest.builder()
                .selectedTopicId(List.of())
                .page(1)
                .size(10)
                .build();

        repository.findByFilters("pkg-1", request);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Topic.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertFalse(documentContainsKey(queryObject, "$nin"));
    }

    @Test
    void findByFilters_withSelectedTopicId_pinsSelectedFirstAndExcludesFromOthers() {
        Topic selected = Topic.builder().id("topic-selected").topicName("Selected Topic").build();
        Topic other = Topic.builder().id("topic-other").topicName("Other Topic").build();

        when(mongoTemplate.find(any(Query.class), eq(Topic.class)))
                .thenReturn(List.of(selected))  // selected query
                .thenReturn(List.of(other));    // paginated others
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(5L);

        TopicFilterRequest request = TopicFilterRequest.builder()
                .selectedTopicId(List.of("topic-selected"))
                .page(1)
                .size(10)
                .build();

        var page = repository.findByFilters("pkg-1", request);

        assertEquals(2, page.getContent().size());
        assertEquals("topic-selected", page.getContent().get(0).getId());
        assertEquals("topic-other", page.getContent().get(1).getId());
        // PageImpl remaps total when offset+pageSize > total on the last page; with pageSize=10
        // and merged content size 2, getTotalElements() becomes 2 (not 5 others + 1 selected).
        assertEquals(2L, page.getTotalElements());

        verify(mongoTemplate, org.mockito.Mockito.times(2)).find(queryCaptor.capture(), eq(Topic.class));
        List<Query> queries = queryCaptor.getAllValues();
        // Second query (others) must exclude selected id
        assertTrue(documentContainsKey(queries.get(1).getQueryObject(), "$nin"));
        assertTrue(documentContainsValue(queries.get(1).getQueryObject(), "topic-selected"));
    }

    @Test
    void findByFilters_withClientId_appliesPrivacyCriteria() {
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Topic.class))).thenReturn(List.of());

        TopicFilterRequest request = TopicFilterRequest.builder()
                .clientId("client-viewer")
                .page(1)
                .size(10)
                .build();

        repository.findByFilters("pkg-1", request);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Topic.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "isPrivate"));
        assertTrue(documentContainsValue(queryObject, "client-viewer"));
        assertTrue(documentContainsKey(queryObject, "$ne") || documentContainsKey(queryObject, "$or"));
    }

    @Test
    void findByFilters_withoutClientId_skipsPrivacyCriteria() {
        when(mongoTemplate.count(any(Query.class), eq(Topic.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Topic.class))).thenReturn(List.of());

        TopicFilterRequest request = TopicFilterRequest.builder()
                .page(1)
                .size(10)
                .build();

        repository.findByFilters("pkg-1", request);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(Topic.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        // Without viewer clientId, privacy branch should not constrain by client-viewer
        assertFalse(documentContainsValue(queryObject, "client-viewer"));
    }

    @Test
    void recommendByProduct_ranksByMatchScoreAndDoesNotHardFailOnIndustry() {
        Topic topic = Topic.builder().id("t1").topicName("Phishing Basics").build();
        Document topicDoc = new Document("_id", "t1").append("topicName", "Phishing Basics").append("matchScore", 3);
        Document facetRoot = new Document()
                .append("metadata", List.of(new Document("total", 1)))
                .append("data", List.of(topicDoc));

        org.springframework.data.mongodb.core.convert.MongoConverter converter =
                org.mockito.Mockito.mock(org.springframework.data.mongodb.core.convert.MongoConverter.class);
        when(mongoTemplate.getConverter()).thenReturn(converter);
        when(converter.read(eq(Topic.class), any(Document.class))).thenReturn(topic);
        when(mongoTemplate.aggregate(
                any(org.springframework.data.mongodb.core.aggregation.Aggregation.class),
                eq(Topic.class),
                eq(Document.class)))
                .thenReturn(new org.springframework.data.mongodb.core.aggregation.AggregationResults<>(
                        List.of(facetRoot), new Document()));

        TopicFilterRequest request = TopicFilterRequest.builder()
                .payloadTypeIds(List.of("payload-1"))
                .difficultyIds(List.of("diff-1"))
                .industryIds(List.of("industry-missing"))
                .tags(List.of("Security"))
                .page(1)
                .size(6)
                .build();

        var page = repository.recommendByProduct("product-1", request);

        assertEquals(1, page.getContent().size());
        assertEquals("t1", page.getContent().get(0).topic().getId());
        assertEquals(3, page.getContent().get(0).matchScore());
        assertEquals(1L, page.getTotalElements());
        verify(mongoTemplate).aggregate(
                any(org.springframework.data.mongodb.core.aggregation.Aggregation.class),
                eq(Topic.class),
                eq(Document.class));
    }

    private static boolean documentContainsKey(Document doc, String key) {
        if (doc.containsKey(key)) {
            return true;
        }
        for (Object value : doc.values()) {
            if (value instanceof Document nested && documentContainsKey(nested, key)) {
                return true;
            }
            if (value instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Document nested && documentContainsKey(nested, key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean documentContainsValue(Document doc, Object needle) {
        for (Object value : doc.values()) {
            if (needle.equals(value)) {
                return true;
            }
            if (value instanceof Document nested && documentContainsValue(nested, needle)) {
                return true;
            }
            if (value instanceof List<?> list) {
                if (list.contains(needle)) {
                    return true;
                }
                for (Object item : list) {
                    if (item instanceof Document nested && documentContainsValue(nested, needle)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
