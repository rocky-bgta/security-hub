package com.aspire.asat.registration.repository.custom.impl;

import com.aspire.asat.registration.model.AspireUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspireUserRepositoryCustomImplEndUserPagedTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AspireUserRepositoryCustomImpl repository;

    @Test
    void findEndUsersPaged_appliesSkipAsOffsetTimesPageSize() {
        when(mongoTemplate.find(any(Query.class), eq(AspireUser.class))).thenReturn(List.of());

        repository.findEndUsersPaged(
                "client-1", null, null, null, null, List.of(), 2, 5);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AspireUser.class));
        assertEquals(10L, captor.getValue().getSkip());
        assertEquals(5, captor.getValue().getLimit());
    }

    @Test
    void findEndUserIds_emptyClientAdminIds_returnsEmptyWithoutQuery() {
        List<String> ids = repository.findEndUserIds(List.of(), "alice", List.of("HR"));

        assertEquals(List.of(), ids);
        verify(mongoTemplate, never()).find(any(Query.class), eq(AspireUser.class));
    }

    @Test
    void findEndUserIds_searchAndDepartment_projectsUserIds() {
        UUID userId = UUID.fromString("a1e35989-5081-447e-ada1-25b978ea316e");
        AspireUser user = AspireUser.builder().userId(userId).build();
        when(mongoTemplate.find(any(Query.class), eq(AspireUser.class))).thenReturn(List.of(user));

        List<String> ids = repository.findEndUserIds(List.of("client-1"), "alice", List.of("HR"));

        assertEquals(List.of(userId.toString()), ids);
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AspireUser.class));
        String queryJson = captor.getValue().getQueryObject().toJson();
        assertTrue(queryJson.contains("client-1"));
        assertTrue(queryJson.contains("USER"));
        assertTrue(queryJson.contains("department"));
        assertTrue(queryJson.contains("email"));
    }

    @Test
    void findEndUserIds_departmentWithSlash_matchesName() {
        when(mongoTemplate.find(any(Query.class), eq(AspireUser.class))).thenReturn(List.of());

        repository.findEndUserIds(List.of("client-1"), null, List.of("Audit/Internal Controls"));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(AspireUser.class));
        String queryJson = captor.getValue().getQueryObject().toJson();
        assertTrue(queryJson.contains("Audit/Internal Controls"));
        assertTrue(queryJson.contains("department"));
    }

    @Test
    void countEndUsersPaged_withExcludeUserIds_usesSameMatch() {
        UUID excluded = UUID.randomUUID();
        when(mongoTemplate.count(any(Query.class), eq(AspireUser.class))).thenReturn(3L);

        long count = repository.countEndUsersPaged(
                "client-1", "alice", "ACTIVE", List.of("HR"), null, List.of(excluded));

        assertEquals(3L, count);
        verify(mongoTemplate).count(any(Query.class), eq(AspireUser.class));
    }
}
