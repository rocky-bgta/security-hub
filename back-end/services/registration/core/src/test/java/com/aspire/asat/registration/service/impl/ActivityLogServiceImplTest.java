package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.activitylog.ActivityLogRequestDto;
import com.aspire.asat.common.dto.activitylog.ActivityLogResponseDto;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.ActivityStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.model.ActivityLog;
import com.aspire.asat.registration.repository.ActivityLogRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceImplTest {

    private static final String MSP_USER_ID = "msp-user-1";
    private static final String MSP_ID = "msp-1";
    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String ASPIRE_ADMIN_ID = "aspire-admin-1";
    private static final String USER_ID = "user-1";

    @Mock
    private ActivityLogRepository activityLogRepository;
    @Mock
    private ClientAdminRepository clientAdminRepository;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ActivityLogServiceImpl activityLogService;

    @BeforeEach
    void setUp() {
        when(mongoTemplate.find(any(Query.class), eq(ActivityLog.class))).thenReturn(List.of(sampleLog()));
        when(mongoTemplate.count(any(Query.class), eq(ActivityLog.class))).thenReturn(1L);
    }

    @Test
    void getActivityLogs_msp_clientAdminIdAll_filtersClientAdminsUnderMsp() {
        ActivityLogResponseDto response = activityLogService.getActivityLogs(
                requestWith(null, "ALL"), mspContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.CLIENT_ADMIN));
        assertTrue(hasFieldEquals(query, "mspId", MSP_USER_ID));
        assertFalse(hasFieldEquals(query, "clientAdminId", CLIENT_ADMIN_ID));
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getActivityLogs().size());
    }

    @Test
    void getActivityLogs_msp_specificClientAdminId_filtersByClientAdminAndMsp() {
        activityLogService.getActivityLogs(requestWith(null, CLIENT_ADMIN_ID), mspContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.CLIENT_ADMIN));
        assertTrue(hasFieldEquals(query, "clientAdminId", CLIENT_ADMIN_ID));
        assertTrue(hasFieldEquals(query, "mspId", MSP_USER_ID));
    }

    @Test
    void getActivityLogs_msp_noClientAdminId_usesDefaultMspScope() {
        activityLogService.getActivityLogs(requestWith(null, null), mspContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "mspId", MSP_ID));
        assertTrue(hasFieldIn(query, "userType", UserType.MSP, UserType.USER, UserType.CLIENT_ADMIN));
    }

    @Test
    void getActivityLogs_aspireAdmin_mspIdAll_filtersByUserTypeMsp() {
        activityLogService.getActivityLogs(requestWith("ALL", null), aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.MSP));
        assertFalse(hasFieldEquals(query, "mspId", "ALL"));
    }

    @Test
    void getActivityLogs_aspireAdmin_specificMspId_filtersByMspId() {
        activityLogService.getActivityLogs(requestWith(MSP_ID, null), aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "mspId", MSP_ID));
    }

    @Test
    void getActivityLogs_aspireAdmin_clientAdminIdAll_filtersByUserTypeClientAdmin() {
        activityLogService.getActivityLogs(requestWith(null, "ALL"), aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.CLIENT_ADMIN));
        assertFalse(hasFieldEquals(query, "clientAdminId", "ALL"));
    }

    @Test
    void getActivityLogs_aspireAdmin_specificClientAdminId_filtersByClientAdmin() {
        activityLogService.getActivityLogs(requestWith(null, CLIENT_ADMIN_ID), aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.CLIENT_ADMIN));
        assertTrue(hasFieldEquals(query, "clientAdminId", CLIENT_ADMIN_ID));
    }

    @Test
    void getActivityLogs_aspireAdmin_mspIdAllAndClientAdminId_prefersClientAdminFilter() {
        activityLogService.getActivityLogs(requestWith("ALL", CLIENT_ADMIN_ID), aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.CLIENT_ADMIN));
        assertTrue(hasFieldEquals(query, "clientAdminId", CLIENT_ADMIN_ID));
        assertFalse(hasFieldEquals(query, "userType", UserType.MSP));
    }

    @Test
    void getActivityLogs_aspireAdmin_specificMspAndClientAdminAll_scopesClientAdminsUnderMsp() {
        activityLogService.getActivityLogs(requestWith(MSP_ID, "ALL"), aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.CLIENT_ADMIN));
        assertTrue(hasFieldEquals(query, "mspId", MSP_ID));
        assertFalse(hasFieldEquals(query, "clientAdminId", "ALL"));
    }

    @Test
    void getActivityLogs_user_scopesToOwnLogs() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId(USER_ID)
                .userType(UserType.USER.name())
                .build();

        activityLogService.getActivityLogs(requestWith(null, null), context);

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.USER));
        assertTrue(hasFieldEquals(query, "userId", USER_ID));
    }

    @Test
    void getActivityLogs_clientAdmin_scopesToClientAdminId() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();

        activityLogService.getActivityLogs(requestWith(null, null), context);

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "clientAdminId", CLIENT_ADMIN_ID));
        assertTrue(hasFieldIn(query, "userType", UserType.USER, UserType.CLIENT_ADMIN));
    }

    @Test
    void getActivityLogs_msp_userAll_filtersByUserTypeUser() {
        activityLogService.getActivityLogs(requestWith(null, null, "ALL"), mspContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.USER));
        assertTrue(hasFieldEquals(query, "mspId", MSP_ID));
    }

    @Test
    void getActivityLogs_msp_specificUser_filtersByUserId() {
        activityLogService.getActivityLogs(requestWith(null, null, USER_ID), mspContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.USER));
        assertTrue(hasFieldEquals(query, "userId", USER_ID));
    }

    @Test
    void getActivityLogs_aspireAdmin_userAll_filtersByUserTypeUser() {
        activityLogService.getActivityLogs(requestWith(null, null, "ALL"), aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.USER));
        assertFalse(hasFieldEquals(query, "userId", "ALL"));
    }

    @Test
    void getActivityLogs_clientAdmin_userAll_filtersByUserTypeUser() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();

        activityLogService.getActivityLogs(requestWith(null, null, "ALL"), context);

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userType", UserType.USER));
        assertTrue(hasFieldEquals(query, "clientAdminId", CLIENT_ADMIN_ID));
    }

    @Test
    void getActivityLogs_aspireAdmin_userIdAndUserTypeMsp_filtersByBoth() {
        ActivityLogRequestDto request = ActivityLogRequestDto.builder()
                .userId(MSP_USER_ID)
                .userType(UserType.MSP)
                .offset(0)
                .pageSize(10)
                .build();

        activityLogService.getActivityLogs(request, aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userId", MSP_USER_ID));
        assertTrue(hasFieldEquals(query, "userType", UserType.MSP));
    }

    @Test
    void getActivityLogs_aspireAdmin_userIdAndUserTypeAspireAdmin_filtersByBoth() {
        ActivityLogRequestDto request = ActivityLogRequestDto.builder()
                .userId(ASPIRE_ADMIN_ID)
                .userType(UserType.ASPIRE_ADMIN)
                .offset(0)
                .pageSize(10)
                .build();

        activityLogService.getActivityLogs(request, aspireAdminContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userId", ASPIRE_ADMIN_ID));
        assertTrue(hasFieldEquals(query, "userType", UserType.ASPIRE_ADMIN));
    }

    @Test
    void getActivityLogs_msp_userIdAndUserTypeClientAdmin_filtersByBoth() {
        ActivityLogRequestDto request = ActivityLogRequestDto.builder()
                .userId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN)
                .offset(0)
                .pageSize(10)
                .build();

        activityLogService.getActivityLogs(request, mspContext());

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userId", CLIENT_ADMIN_ID));
        assertTrue(hasFieldEquals(query, "userType", UserType.CLIENT_ADMIN));
        assertTrue(hasFieldEquals(query, "mspId", MSP_ID));
    }

    @Test
    void getActivityLogs_clientAdmin_userFilter_filtersByEndUserAndClientAdminScope() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();

        // End-user filtering for client admins uses request.user (not userId+userType).
        ActivityLogRequestDto request = ActivityLogRequestDto.builder()
                .user(USER_ID)
                .offset(0)
                .pageSize(10)
                .build();

        activityLogService.getActivityLogs(request, context);

        Document query = captureFindQuery();
        assertTrue(hasFieldEquals(query, "userId", USER_ID));
        assertTrue(hasFieldEquals(query, "userType", UserType.USER));
        assertTrue(hasFieldEquals(query, "clientAdminId", CLIENT_ADMIN_ID));
    }

    @Test
    void getActivityLogs_userIdWithoutUserType_doesNotApplyUserIdFilter() {
        ActivityLogRequestDto request = ActivityLogRequestDto.builder()
                .userId(MSP_USER_ID)
                .offset(0)
                .pageSize(10)
                .build();

        activityLogService.getActivityLogs(request, aspireAdminContext());

        Document query = captureFindQuery();
        assertFalse(hasFieldEquals(query, "userId", MSP_USER_ID));
    }

    private Document captureFindQuery() {
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(ActivityLog.class));
        return queryCaptor.getValue().getQueryObject();
    }

    private static boolean hasFieldEquals(Document query, String field, Object expected) {
        for (Document clause : flattenClauses(query)) {
            Object actual = clause.get(field);
            if (actual == null) {
                continue;
            }
            if (valuesEqual(actual, expected)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFieldIn(Document query, String field, Object... expectedValues) {
        for (Document clause : flattenClauses(query)) {
            Object actual = clause.get(field);
            if (actual instanceof Document inDoc && inDoc.containsKey("$in")) {
                Collection<?> values = inDoc.getList("$in", Object.class);
                for (Object expected : expectedValues) {
                    boolean found = values.stream().anyMatch(v -> valuesEqual(v, expected));
                    if (!found) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static List<Document> flattenClauses(Document query) {
        List<Document> clauses = new ArrayList<>();
        if (query.containsKey("$and")) {
            clauses.addAll(query.getList("$and", Document.class));
        } else {
            clauses.add(query);
        }
        return clauses;
    }

    private static boolean valuesEqual(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return actual == expected;
        }
        if (actual instanceof Enum<?> actualEnum && expected instanceof Enum<?> expectedEnum) {
            return actualEnum.name().equals(expectedEnum.name());
        }
        if (actual instanceof Enum<?> actualEnum) {
            return actualEnum.name().equals(String.valueOf(expected));
        }
        if (expected instanceof Enum<?> expectedEnum) {
            return String.valueOf(actual).equals(expectedEnum.name());
        }
        return String.valueOf(actual).equals(String.valueOf(expected));
    }

    private static ActivityLogRequestDto requestWith(String mspId, String clientAdminId) {
        return requestWith(mspId, clientAdminId, null);
    }

    private static ActivityLogRequestDto requestWith(String mspId, String clientAdminId, String user) {
        return ActivityLogRequestDto.builder()
                .mspId(mspId)
                .clientAdminId(clientAdminId)
                .user(user)
                .offset(0)
                .pageSize(10)
                .sortBy("createdAt")
                .order("desc")
                .build();
    }

    private static CurrentUserContext mspContext() {
        return CurrentUserContext.builder()
                .userId(MSP_USER_ID)
                .mspId(MSP_ID)
                .userType(UserType.MSP.name())
                .build();
    }

    private static CurrentUserContext aspireAdminContext() {
        return CurrentUserContext.builder()
                .userId(ASPIRE_ADMIN_ID)
                .userType(UserType.ASPIRE_ADMIN.name())
                .build();
    }

    private static ActivityLog sampleLog() {
        return ActivityLog.builder()
                .id("log-1")
                .userId(USER_ID)
                .mspId(MSP_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN)
                .activityType(ActivityType.CLIENT_CREATED)
                .activityStatus(ActivityStatus.SUCCESS)
                .description("Client created")
                .username("admin")
                .email("admin@example.com")
                .fullName("Admin User")
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
    }
}
