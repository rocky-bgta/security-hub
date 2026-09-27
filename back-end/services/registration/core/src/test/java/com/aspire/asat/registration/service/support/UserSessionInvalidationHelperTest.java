package com.aspire.asat.registration.service.support;

import com.aspire.asat.registration.client.service.AuthServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserSessionInvalidationHelperTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private UserSessionInvalidationHelper helper;

    @Test
    void isRestrictiveStatus_trueForInactiveSuspendBlocked() {
        assertTrue(helper.isRestrictiveStatus("INACTIVE"));
        assertTrue(helper.isRestrictiveStatus("SUSPEND"));
        assertTrue(helper.isRestrictiveStatus("TEMPORARY_BLOCKED"));
        assertTrue(helper.isRestrictiveStatus("blocked"));
        assertFalse(helper.isRestrictiveStatus("ACTIVE"));
        assertFalse(helper.isRestrictiveStatus(null));
    }

    @Test
    void logoutUsersIfRestrictive_callsAuthForEachDistinctUserId() {
        helper.logoutUsersIfRestrictive("SUSPEND", Arrays.asList("u1", "u1", "u2", " ", null));

        verify(authServiceClient).logoutUser("u1");
        verify(authServiceClient).logoutUser("u2");
    }

    @Test
    void logoutUsersIfRestrictive_skipsWhenStatusIsActive() {
        helper.logoutUsersIfRestrictive("ACTIVE", List.of("u1"));

        verifyNoInteractions(authServiceClient);
    }

    @Test
    void logoutUsersIfRestrictive_skipsWhenUserIdsEmpty() {
        helper.logoutUsersIfRestrictive("INACTIVE", List.of());

        verify(authServiceClient, never()).logoutUser(org.mockito.ArgumentMatchers.anyString());
    }
}
