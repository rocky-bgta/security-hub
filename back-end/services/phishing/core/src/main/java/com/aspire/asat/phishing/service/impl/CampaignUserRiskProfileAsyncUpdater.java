package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.UserRiskProfileSaveRequestDto;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.UserRiskProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignUserRiskProfileAsyncUpdater {

    private final UserRiskProfileService userRiskProfileService;
    private final UserRiskProfileRepository userRiskProfileRepository;

    @Async
    public void enablePhishingForExistingProfile(String userId, String clientId) {
        if (userId == null || userId.isBlank() || clientId == null || clientId.isBlank()) {
            return;
        }

        try {

            UserRiskProfileSaveRequestDto request = UserRiskProfileSaveRequestDto.builder()
                    .clientId(clientId)
                    .userId(userId)
                    .isPhishingEnabled(true)
                    .fromPhishingContext(true)
                    .build();
            userRiskProfileService.save(request);
        } catch (Exception e) {
            log.error(
                    "Failed async phishing-enable update for user risk profile. clientId={}, userId={}: {}",
                    clientId, userId, e.getMessage(), e
            );
        }
    }
}
