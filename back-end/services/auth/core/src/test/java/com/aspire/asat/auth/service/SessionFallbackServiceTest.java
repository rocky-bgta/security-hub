package com.aspire.asat.auth.service;

import com.aspire.asat.auth.entity.AuthSession;
import com.aspire.asat.auth.entity.AuthTempToken;
import com.aspire.asat.auth.entity.redis.RedisAccessToken;
import com.aspire.asat.auth.repository.AuthSessionRepository;
import com.aspire.asat.auth.repository.AuthTempTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionFallbackServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private AuthTempTokenRepository authTempTokenRepository;

    @InjectMocks
    private SessionFallbackService sessionFallbackService;

    @BeforeEach
    void setUp() throws Exception {
        // @Value is not processed by @InjectMocks — set the field directly via reflection.
        Field field = SessionFallbackService.class.getDeclaredField("fallbackEnabled");
        field.setAccessible(true);
        field.set(sessionFallbackService, true);
    }

    // -------------------------------------------------------------------------
    // saveSession
    // -------------------------------------------------------------------------

    @Test
    void saveSession_WhenFallbackEnabled_PersistsToMongo() {
        RedisAccessToken token = buildToken();
        List<String> permissions = List.of("USR-MGT:VIEW");
        when(authSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sessionFallbackService.saveSession(token, permissions, 20L);

        ArgumentCaptor<AuthSession> captor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository).save(captor.capture());
        AuthSession saved = captor.getValue();
        assertEquals("token-123", saved.getTokenId());
        assertEquals("user-abc", saved.getUserId());
        assertEquals(permissions, saved.getPermissions());
        assertNotNull(saved.getExpiresAt());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void saveSession_WhenFallbackDisabled_DoesNotWriteToMongo() throws Exception {
        Field field = SessionFallbackService.class.getDeclaredField("fallbackEnabled");
        field.setAccessible(true);
        field.set(sessionFallbackService, false);

        sessionFallbackService.saveSession(buildToken(), List.of(), 20L);

        verifyNoInteractions(authSessionRepository);
    }

    @Test
    void saveSession_WhenMongoThrows_ExceptionPropagates() {
        when(authSessionRepository.save(any())).thenThrow(new RuntimeException("Mongo down"));

        assertThrows(RuntimeException.class,
                () -> sessionFallbackService.saveSession(buildToken(), List.of(), 20L));
    }

    // -------------------------------------------------------------------------
    // revokeAllSessionsForUser
    // -------------------------------------------------------------------------

    @Test
    void revokeAllSessionsForUser_DeletesByUserId() {
        sessionFallbackService.revokeAllSessionsForUser("user-abc");

        verify(authSessionRepository).deleteAllByUserId("user-abc");
    }

    @Test
    void revokeAllSessionsForUser_WhenMongoThrows_DoesNotPropagateException() {
        doThrow(new RuntimeException("Mongo error")).when(authSessionRepository).deleteAllByUserId(any());

        assertDoesNotThrow(() -> sessionFallbackService.revokeAllSessionsForUser("user-abc"));
    }

    // -------------------------------------------------------------------------
    // MFA temp token
    // -------------------------------------------------------------------------

    @Test
    void saveTempToken_PersistsDocument() {
        UUID userId = UUID.randomUUID();
        when(authTempTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sessionFallbackService.saveTempToken("jwt.token.string", userId, 5);

        ArgumentCaptor<AuthTempToken> captor = ArgumentCaptor.forClass(AuthTempToken.class);
        verify(authTempTokenRepository).save(captor.capture());
        assertEquals("jwt.token.string", captor.getValue().getToken());
        assertEquals(userId.toString(), captor.getValue().getUserId());
        assertTrue(captor.getValue().getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void validateTempToken_WhenTokenExistsAndNotExpired_ReturnsTrue() {
        String userId = UUID.randomUUID().toString();
        AuthTempToken doc = new AuthTempToken()
                .setToken("t")
                .setUserId(userId)
                .setExpiresAt(Instant.now().plusSeconds(300));
        when(authTempTokenRepository.findById("t")).thenReturn(Optional.of(doc));

        assertTrue(sessionFallbackService.validateTempToken("t", userId));
    }

    @Test
    void validateTempToken_WhenTokenExpired_ReturnsFalse() {
        String userId = UUID.randomUUID().toString();
        AuthTempToken doc = new AuthTempToken()
                .setToken("t")
                .setUserId(userId)
                .setExpiresAt(Instant.now().minusSeconds(10));
        when(authTempTokenRepository.findById("t")).thenReturn(Optional.of(doc));

        assertFalse(sessionFallbackService.validateTempToken("t", userId));
    }

    @Test
    void validateTempToken_WhenTokenNotFound_ReturnsFalse() {
        when(authTempTokenRepository.findById(any())).thenReturn(Optional.empty());

        assertFalse(sessionFallbackService.validateTempToken("missing", "uid"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private RedisAccessToken buildToken() {
        return new RedisAccessToken()
                .setTokenId("token-123")
                .setUserId("user-abc")
                .setAccessToken("checksum-access")
                .setRefreshToken("checksum-refresh")
                .setUsername("tester")
                .setUserType("USER");
    }
}
