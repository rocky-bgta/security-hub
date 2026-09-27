package com.aspire.asat.cms.service.reports.impl;

import com.aspire.asat.cms.dto.reports.TrainingCertificateCountResponseDto;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingCertificateCountServiceImplTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";

    @Mock
    private UserSubPackageRepository userSubPackageRepository;
    @Mock
    private UserCertificateRepository userCertificateRepository;

    @InjectMocks
    private TrainingCertificateCountServiceImpl trainingCertificateCountService;

    @Test
    void getTrainingCertificateCounts_returnsCounts() {
        when(userSubPackageRepository.countByClientAdminIdAndStatus(CLIENT_ADMIN_ID, "COMPLETED")).thenReturn(3L);
        when(userCertificateRepository.countByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(2L);

        TrainingCertificateCountResponseDto result =
                trainingCertificateCountService.getTrainingCertificateCounts(CLIENT_ADMIN_ID);

        assertEquals(3L, result.getTrainingCompletedCount());
        assertEquals(2L, result.getCertificateEarnedCount());
        verify(userSubPackageRepository).countByClientAdminIdAndStatus(CLIENT_ADMIN_ID, "COMPLETED");
        verify(userCertificateRepository).countByClientAdminId(CLIENT_ADMIN_ID);
    }

    @Test
    void getTrainingCertificateCounts_throwsWhenClientAdminIdMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingCertificateCountService.getTrainingCertificateCounts(" "));
    }
}
