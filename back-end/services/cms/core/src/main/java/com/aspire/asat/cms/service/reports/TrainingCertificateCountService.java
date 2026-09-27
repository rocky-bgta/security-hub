package com.aspire.asat.cms.service.reports;

import com.aspire.asat.cms.dto.reports.TrainingCertificateCountResponseDto;

public interface TrainingCertificateCountService {

    /**
     * Returns training-completed (user_subpackages status=COMPLETED) and
     * certificate-earned (user_certificates) counts for the given client admin.
     */
    TrainingCertificateCountResponseDto getTrainingCertificateCounts(String clientAdminId);
}
