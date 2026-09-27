package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.model.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainRepositoryCustomImplTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private MongoTemplate mongoTemplate;

    private DomainRepositoryCustomImpl repository;

    @BeforeEach
    void setUp() {
        repository = new DomainRepositoryCustomImpl(mongoTemplate);
    }

    @Test
    void findWithFilters_noStatus_appliesVisibilityOnly() {
        when(mongoTemplate.find(any(Query.class), eq(Domain.class))).thenReturn(Collections.emptyList());

        repository.findWithFilters(CLIENT_ID, null, null, PageRequest.of(0, 10));

        String queryString = captureFindQueryString();
        assertVisibilityCriteria(queryString);
        assertFalse(queryString.contains("status"));
    }

    @Test
    void findWithFilters_singleStatus_appliesStatusInFilter() {
        when(mongoTemplate.find(any(Query.class), eq(Domain.class))).thenReturn(Collections.emptyList());

        repository.findWithFilters(CLIENT_ID, null, List.of(DomainStatus.VERIFIED), PageRequest.of(0, 10));

        String queryString = captureFindQueryString();
        assertStatusIn(queryString, "VERIFIED");
    }

    @Test
    void findWithFilters_multipleStatuses_appliesStatusInFilter() {
        when(mongoTemplate.find(any(Query.class), eq(Domain.class))).thenReturn(Collections.emptyList());

        repository.findWithFilters(CLIENT_ID, null,
                List.of(DomainStatus.VERIFIED, DomainStatus.VERIFIED_AND_LOCKED),
                PageRequest.of(0, 10));

        String queryString = captureFindQueryString();
        assertStatusIn(queryString, "VERIFIED", "VERIFIED_AND_LOCKED");
    }

    @Test
    void findWithFilters_searchAndStatuses_appliesBothFilters() {
        when(mongoTemplate.find(any(Query.class), eq(Domain.class))).thenReturn(Collections.emptyList());

        repository.findWithFilters(CLIENT_ID, "aspire", List.of(DomainStatus.VERIFIED), PageRequest.of(0, 10));

        String queryString = captureFindQueryString();
        assertTrue(queryString.contains("aspire"));
        assertStatusIn(queryString, "VERIFIED");
    }

    @Test
    void countWithFilters_usesSameCriteriaAsFind() {
        when(mongoTemplate.count(any(Query.class), eq(Domain.class))).thenReturn(3L);

        repository.countWithFilters(CLIENT_ID, "cloud",
                List.of(DomainStatus.VERIFIED, DomainStatus.UNVERIFIED));

        String queryString = captureCountQueryString();
        assertTrue(queryString.contains("cloud"));
        assertStatusIn(queryString, "VERIFIED", "UNVERIFIED");
        assertVisibilityCriteria(queryString);
    }

    private String captureFindQueryString() {
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Domain.class));
        return captor.getValue().toString();
    }

    private String captureCountQueryString() {
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(Domain.class));
        return captor.getValue().toString();
    }

    private void assertVisibilityCriteria(String queryString) {
        assertTrue(queryString.contains("clientId"));
        assertTrue(queryString.contains("isGlobal"));
    }

    private void assertStatusIn(String queryString, String... expectedStatuses) {
        assertTrue(queryString.contains("status"));
        for (String status : expectedStatuses) {
            assertTrue(queryString.contains(status), "Expected status " + status + " in query: " + queryString);
        }
    }
}
