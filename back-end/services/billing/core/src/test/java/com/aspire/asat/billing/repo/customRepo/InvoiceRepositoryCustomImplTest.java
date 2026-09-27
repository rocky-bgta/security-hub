package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvoiceRepositoryCustomImplTest {

    private static final String PRODUCT_ID = "prod-catalog-1";

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private InvoiceRepositoryCustomImpl repository;

    @Captor
    private ArgumentCaptor<Query> queryCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void countInvoicesWithDynamicFilters_productId_includesProductSelectionsCriteria() {
        when(mongoTemplate.count(any(Query.class), any(Class.class))).thenReturn(3L);

        long count = repository.countInvoicesWithDynamicFilters(
                null, null, null, List.of(InvoiceStatus.PENDING), null, null, null, null, PRODUCT_ID);

        assertEquals(3L, count);
        verify(mongoTemplate).count(queryCaptor.capture(), any(Class.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "productSelections.productId"));
        assertTrue(documentContainsValue(queryObject, PRODUCT_ID));
    }

    @Test
    void countInvoicesWithDynamicFilters_noProductId_preservesExistingCriteriaOnly() {
        when(mongoTemplate.count(any(Query.class), any(Class.class))).thenReturn(5L);

        long count = repository.countInvoicesWithDynamicFilters(
                "client-1", null, null, null, null, null, null, null, null);

        assertEquals(5L, count);
        verify(mongoTemplate).count(queryCaptor.capture(), any(Class.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "clientAdminId"));
        assertTrue(documentContainsValue(queryObject, "client-1"));
    }

    private static boolean documentContainsKey(Document document, String key) {
        if (document.containsKey(key)) {
            return true;
        }
        for (Object value : document.values()) {
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

    private static boolean documentContainsValue(Document document, Object expected) {
        for (Object value : document.values()) {
            if (expected.equals(value)) {
                return true;
            }
            if (value instanceof Document nested && documentContainsValue(nested, expected)) {
                return true;
            }
            if (value instanceof List<?> list) {
                for (Object item : list) {
                    if (expected.equals(item)) {
                        return true;
                    }
                    if (item instanceof Document nested && documentContainsValue(nested, expected)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
