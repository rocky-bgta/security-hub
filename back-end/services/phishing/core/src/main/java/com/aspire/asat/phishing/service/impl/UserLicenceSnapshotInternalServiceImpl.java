package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.UserLicenceSnapshotRequest;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.repository.PhishingUserLicenceRepository;
import com.aspire.asat.phishing.service.UserLicenceSnapshotInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLicenceSnapshotInternalServiceImpl implements UserLicenceSnapshotInternalService {

    private final PhishingUserLicenceRepository phishingUserLicenceRepository;

    @Override
    public long syncUserSnapshot(UserLicenceSnapshotRequest request) {
        if (request == null) {
            throw new PhishingValidationException("request is required");
        }
        if (!StringUtils.hasText(request.getUserId())) {
            throw new PhishingValidationException("userId is required");
        }
        if (!StringUtils.hasText(request.getClientAdminId())) {
            throw new PhishingValidationException("clientAdminId is required");
        }

        long matched = phishingUserLicenceRepository.updateUserSnapshot(
                request.getUserId().trim(),
                request.getClientAdminId().trim(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getDepartmentName(),
                request.getCountryName(),
                request.getActive());

        log.info("Synced licence snapshot for userId={}, clientAdminId={}, matched={}",
                request.getUserId(), request.getClientAdminId(), matched);
        return matched;
    }
}
