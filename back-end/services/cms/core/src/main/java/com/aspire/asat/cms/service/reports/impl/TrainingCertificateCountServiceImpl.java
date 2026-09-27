package com.aspire.asat.cms.service.reports.impl;

import com.aspire.asat.cms.dto.reports.TrainingCertificateCountResponseDto;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.service.reports.TrainingCertificateCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainingCertificateCountServiceImpl implements TrainingCertificateCountService {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final UserSubPackageRepository userSubPackageRepository;
    private final UserCertificateRepository userCertificateRepository;

    @Override
    public TrainingCertificateCountResponseDto getTrainingCertificateCounts(String clientAdminId) {
        if (!StringUtils.hasText(clientAdminId)) {
            throw new IllegalArgumentException("clientAdminId is required");
        }

        String resolvedId = clientAdminId.trim();
        long trainingCompleted = userSubPackageRepository.countByClientAdminIdAndStatus(resolvedId, STATUS_COMPLETED);
        long certificatesEarned = userCertificateRepository.countByClientAdminId(resolvedId);

        log.info("Training/certificate counts for clientAdminId {}: completed={}, certificates={}",
                resolvedId, trainingCompleted, certificatesEarned);

        return TrainingCertificateCountResponseDto.builder()
                .trainingCompletedCount(trainingCompleted)
                .certificateEarnedCount(certificatesEarned)
                .build();
    }
}
