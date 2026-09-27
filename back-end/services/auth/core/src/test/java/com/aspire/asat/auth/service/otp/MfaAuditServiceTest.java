package com.aspire.asat.auth.service.otp;

import com.aspire.asat.auth.dto.enums.MfaAuditEventType;
import com.aspire.asat.auth.entity.MfaAuditLog;
import com.aspire.asat.auth.repository.MfaAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaAuditServiceTest {

    @Mock
    private MfaAuditLogRepository auditLogRepository;

    @InjectMocks
    private MfaAuditService mfaAuditService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void record_PersistsEventWithoutOtp() {
        when(auditLogRepository.save(any(MfaAuditLog.class))).thenAnswer(inv -> inv.getArgument(0));
        UUID sessionId = UUID.randomUUID();

        mfaAuditService.record(MfaAuditEventType.OTP_GENERATED, userId, "EMAIL", sessionId,
                "10.0.0.1", MfaAuditService.OUTCOME_SUCCESS, null, Map.of("resendCount", 0));

        ArgumentCaptor<MfaAuditLog> captor = ArgumentCaptor.forClass(MfaAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        MfaAuditLog saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals("OTP_GENERATED", saved.getEventType());
        assertEquals("EMAIL", saved.getMethod());
        assertEquals(sessionId, saved.getSessionId());
        assertEquals("10.0.0.1", saved.getIpAddress());
        assertEquals(MfaAuditService.OUTCOME_SUCCESS, saved.getOutcome());
        assertNull(saved.getReason());
        assertEquals(0, saved.getMetadata().get("resendCount"));
    }

    @Test
    void record_RejectsMetadataContainingOtp() {
        assertThrows(IllegalArgumentException.class, () ->
                mfaAuditService.record(MfaAuditEventType.OTP_GENERATED, userId, "EMAIL", null,
                        "10.0.0.1", MfaAuditService.OUTCOME_SUCCESS, null, Map.of("otp", "123456")));
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    void record_SwallowsRepositoryFailure() {
        when(auditLogRepository.save(any())).thenThrow(new RuntimeException("mongo down"));

        mfaAuditService.record(MfaAuditEventType.OTP_LOCKED, userId, "EMAIL", null,
                "10.0.0.1", MfaAuditService.OUTCOME_DENIED, "max_attempts", Map.of());

        verify(auditLogRepository).save(any(MfaAuditLog.class));
    }
}
