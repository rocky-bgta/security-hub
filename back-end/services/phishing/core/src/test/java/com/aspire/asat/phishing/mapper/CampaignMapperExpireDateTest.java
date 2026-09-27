package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.enums.CampaignExpiryValidityUnit;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.request.CampaignExpireDateRequest;
import com.aspire.asat.phishing.dto.response.CampaignDto;
import com.aspire.asat.phishing.model.Campaign;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CampaignMapperExpireDateTest {

    private final CampaignMapper campaignMapper = new CampaignMapper();

    @Test
    void toDtoShouldMapExpireDateAndKeepExpiresAt() {
        CampaignExpireDateRequest expireDate = CampaignExpireDateRequest.builder()
                .validityUnit(CampaignExpiryValidityUnit.DAYS)
                .validityPeriod(1)
                .build();
        Instant expiresAt = Instant.parse("2026-05-30T10:00:00Z");

        Campaign campaign = Campaign.builder()
                .id("cmp-1")
                .campaignName("Campaign A")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.DRAFT)
                .expireDate(expireDate)
                .expiresAt(expiresAt)
                .currentStep(1)
                .build();

        CampaignDto dto = campaignMapper.toDto(campaign);

        assertEquals(expireDate, dto.getExpireDate());
        assertEquals(expiresAt, dto.getExpiresAt());
    }

    @Test
    void toDtoShouldReturnNullExpireDateForLegacyCampaignWithoutIt() {
        Instant expiresAt = Instant.parse("2026-06-01T00:00:00Z");
        Campaign campaign = Campaign.builder()
                .id("cmp-legacy")
                .campaignName("Legacy")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.DRAFT)
                .expiresAt(expiresAt)
                .currentStep(1)
                .build();

        CampaignDto dto = campaignMapper.toDto(campaign);

        assertNull(dto.getExpireDate());
        assertEquals(expiresAt, dto.getExpiresAt());
    }
}
