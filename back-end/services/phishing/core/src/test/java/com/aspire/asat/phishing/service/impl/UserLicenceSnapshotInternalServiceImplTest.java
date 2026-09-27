package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.UserLicenceSnapshotRequest;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.repository.PhishingUserLicenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLicenceSnapshotInternalServiceImplTest {

    @Mock
    private PhishingUserLicenceRepository phishingUserLicenceRepository;

    @InjectMocks
    private UserLicenceSnapshotInternalServiceImpl service;

    @Test
    void syncUserSnapshot_delegatesToRepository() {
        UserLicenceSnapshotRequest request = UserLicenceSnapshotRequest.builder()
                .userId("user-1")
                .clientAdminId("client-1")
                .firstName("Ada")
                .lastName("Lovelace")
                .phoneNumber("123")
                .departmentName("HR")
                .countryName("Bangladesh")
                .active(true)
                .build();
        when(phishingUserLicenceRepository.updateUserSnapshot(
                eq("user-1"), eq("client-1"), eq("Ada"), eq("Lovelace"),
                eq("123"), eq("HR"), eq("Bangladesh"), eq(true)))
                .thenReturn(2L);

        assertEquals(2L, service.syncUserSnapshot(request));
        verify(phishingUserLicenceRepository).updateUserSnapshot(
                "user-1", "client-1", "Ada", "Lovelace", "123", "HR", "Bangladesh", true);
    }

    @Test
    void syncUserSnapshot_blankUserId_throws() {
        UserLicenceSnapshotRequest request = UserLicenceSnapshotRequest.builder()
                .userId(" ")
                .clientAdminId("client-1")
                .build();

        assertThrows(PhishingValidationException.class, () -> service.syncUserSnapshot(request));
    }

    @Test
    void syncUserSnapshot_zeroMatches_isSuccess() {
        UserLicenceSnapshotRequest request = UserLicenceSnapshotRequest.builder()
                .userId("user-1")
                .clientAdminId("client-1")
                .firstName("Ada")
                .build();
        when(phishingUserLicenceRepository.updateUserSnapshot(
                eq("user-1"), eq("client-1"), eq("Ada"), isNull(),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(0L);

        assertEquals(0L, service.syncUserSnapshot(request));
    }
}
