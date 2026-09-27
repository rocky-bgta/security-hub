package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.model.SubPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubPackageRepositoryCustomImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private SubPackageRepositoryCustomImpl repository;

    @Test
    void countSubPackagesForReport_shouldScopeByClientAdminId() {
        when(mongoTemplate.count(any(Query.class), eq(SubPackage.class))).thenReturn(8L);

        long count = repository.countSubPackagesForReport("client-1", null);

        assertEquals(8L, count);
        verify(mongoTemplate).count(any(Query.class), eq(SubPackage.class));
    }

    @Test
    void countSubPackagesForReport_shouldScopeByClientAdminIds() {
        when(mongoTemplate.count(any(Query.class), eq(SubPackage.class))).thenReturn(12L);

        long count = repository.countSubPackagesForReport(null, List.of("client-1", "client-2"));

        assertEquals(12L, count);
        verify(mongoTemplate).count(any(Query.class), eq(SubPackage.class));
    }

    @Test
    void countSubPackagesWithFilters_shouldScopeByClientAdminIds() {
        when(mongoTemplate.count(any(Query.class), eq(SubPackage.class))).thenReturn(5L);

        long count = repository.countSubPackagesWithFilters(
                null, null, null, null, List.of("ca-1", "ca-2"));

        assertEquals(5L, count);
        verify(mongoTemplate).count(any(Query.class), eq(SubPackage.class));
    }
}
