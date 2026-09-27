package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.UserCertificate;
import com.aspire.asat.cms.repository.ProductRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCertificateRepositoryCustomImplCertificateDetailsTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String PRODUCT_ID = "product-1";

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UserCertificateRepositoryCustomImpl repository;

    @Captor
    private ArgumentCaptor<Query> queryCaptor;

    @Test
    void countByClientAdminIdWithSearch_noOptionalFilters_onlyScopesByClientAdminId() {
        when(mongoTemplate.count(any(Query.class), eq(UserCertificate.class))).thenReturn(5L);

        long count = repository.countByClientAdminIdWithSearch(
                CLIENT_ADMIN_ID, null, null, null);

        assertEquals(5L, count);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(UserCertificate.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "clientAdminId"));
        assertTrue(documentContainsValue(queryObject, CLIENT_ADMIN_ID));
    }

    @Test
    void countByClientAdminIdWithSearch_certificateNameSearch_preservesExistingSearchBehavior() {
        when(mongoTemplate.count(any(Query.class), eq(UserCertificate.class))).thenReturn(1L);

        repository.countByClientAdminIdWithSearch(
                CLIENT_ADMIN_ID, "trial", null, null);

        verify(mongoTemplate).count(queryCaptor.capture(), eq(UserCertificate.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "productName"));
        assertTrue(documentContainsKey(queryObject, "certificateId"));
    }

    @Test
    void countByClientAdminIdWithSearch_productId_resolvesProductName() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setProductName("Security Awareness");
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(mongoTemplate.count(any(Query.class), eq(UserCertificate.class))).thenReturn(2L);

        long count = repository.countByClientAdminIdWithSearch(
                CLIENT_ADMIN_ID, null, PRODUCT_ID, null);

        assertEquals(2L, count);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(UserCertificate.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsValue(queryObject, "Security Awareness"));
        assertTrue(documentContainsKey(queryObject, "clientAdminId"));
    }

    @Test
    void countByClientAdminIdWithSearch_invalidProductId_returnsNoMatch() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        when(mongoTemplate.count(any(Query.class), eq(UserCertificate.class))).thenReturn(0L);

        long count = repository.countByClientAdminIdWithSearch(
                CLIENT_ADMIN_ID, null, PRODUCT_ID, null);

        assertEquals(0L, count);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(UserCertificate.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsValue(queryObject, "__no_match__"));
    }

    @Test
    void findByClientAdminIdWithSearch_statusFilter_includesExpiryCriteria() {
        when(mongoTemplate.count(any(Query.class), eq(UserCertificate.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(UserCertificate.class))).thenReturn(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        repository.findByClientAdminIdWithSearch(
                CLIENT_ADMIN_ID, null, null, CertificateStatus.EXPIRED, pageable);

        verify(mongoTemplate).count(queryCaptor.capture(), eq(UserCertificate.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "expiryDate"));
    }

    @Test
    void findCertificatesForReport_noStatusOnExpiryDate_excludesValidBeyondThreshold() {
        when(mongoTemplate.find(any(Query.class), eq(UserCertificate.class))).thenReturn(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        repository.findCertificatesForReport(
                CLIENT_ADMIN_ID, null, null, null, null, null, 60, "expiryDate", pageable);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(UserCertificate.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        String queryText = queryObject.toString();
        assertTrue(documentContainsKey(queryObject, "expiryDate"));
        assertTrue(queryText.contains("$lte") || queryText.contains("$or"),
                "Unfiltered expired report must restrict to expiryDate <= threshold (or null)");
    }

    @Test
    void findCertificatesForReport_noStatusOnCreatedAt_doesNotApplyExpiryWindow() {
        when(mongoTemplate.find(any(Query.class), eq(UserCertificate.class))).thenReturn(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        repository.findCertificatesForReport(
                CLIENT_ADMIN_ID, null, null, null, null, null, 60, "createdAt", pageable);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(UserCertificate.class));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "clientAdminId"));
        assertFalse(documentContainsKey(queryObject, "expiryDate"),
                "Issued report must not default to an expiryDate window");
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
