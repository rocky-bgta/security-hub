package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.response.CampaignLicenseUsageDto;
import com.aspire.asat.phishing.repository.custom.CampaignLicenseUsageRepositoryCustom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignLicenseUsageServiceImplTest {

    private static final String CLIENT_ID = "client-1";
    private static final String PP_SILVER = "pp-silver-1";
    private static final String PP_GOLD = "pp-gold-1";

    @Mock
    private CampaignLicenseUsageRepositoryCustom campaignLicenseUsageRepositoryCustom;

    @InjectMocks
    private CampaignLicenseUsageServiceImpl service;

    @Test
    void getCampaignLicenseUsage_example1_firstCampaignTenUsers() {
        when(campaignLicenseUsageRepositoryCustom.countUniqueUsersByProductPackageId(CLIENT_ID, PP_SILVER))
                .thenReturn(List.of(usage(PP_SILVER, 10)));

        List<CampaignLicenseUsageDto> result = service.getCampaignLicenseUsage(CLIENT_ID, PP_SILVER);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getUniqueUserCount());
        assertEquals(PP_SILVER, result.get(0).getProductPackageId());
    }

    @Test
    void getCampaignLicenseUsage_example2_sameUsersNotDoubleCounted() {
        when(campaignLicenseUsageRepositoryCustom.countUniqueUsersByProductPackageId(CLIENT_ID, PP_SILVER))
                .thenReturn(List.of(usage(PP_SILVER, 10)));

        List<CampaignLicenseUsageDto> result = service.getCampaignLicenseUsage(CLIENT_ID, PP_SILVER);

        assertEquals(10, result.get(0).getUniqueUserCount());
    }

    @Test
    void getCampaignLicenseUsage_example3_fiveNewUsersIncreaseToFifteen() {
        when(campaignLicenseUsageRepositoryCustom.countUniqueUsersByProductPackageId(CLIENT_ID, PP_SILVER))
                .thenReturn(List.of(usage(PP_SILVER, 15)));

        List<CampaignLicenseUsageDto> result = service.getCampaignLicenseUsage(CLIENT_ID, PP_SILVER);

        assertEquals(15, result.get(0).getUniqueUserCount());
    }

    @Test
    void getCampaignLicenseUsage_differentAssignmentsAreIsolated() {
        when(campaignLicenseUsageRepositoryCustom.countUniqueUsersByProductPackageId(CLIENT_ID, null))
                .thenReturn(List.of(
                        usage(PP_SILVER, 10),
                        usage(PP_GOLD, 5)));

        List<CampaignLicenseUsageDto> result = service.getCampaignLicenseUsage(CLIENT_ID, null);

        assertEquals(2, result.size());
        int total = result.stream().mapToInt(CampaignLicenseUsageDto::getUniqueUserCount).sum();
        assertEquals(15, total);
    }

    @Test
    void getCampaignLicenseUsage_emptyClient_returnsEmpty() {
        when(campaignLicenseUsageRepositoryCustom.countUniqueUsersByProductPackageId(CLIENT_ID, null))
                .thenReturn(List.of());

        List<CampaignLicenseUsageDto> result = service.getCampaignLicenseUsage(CLIENT_ID, null);

        assertTrue(result.isEmpty());
        verify(campaignLicenseUsageRepositoryCustom).countUniqueUsersByProductPackageId(eq(CLIENT_ID), isNull());
    }

    @Test
    void getCampaignLicenseUsage_blankClientId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getCampaignLicenseUsage("  ", PP_SILVER));
    }

    private static CampaignLicenseUsageDto usage(String productPackageId, int count) {
        return CampaignLicenseUsageDto.builder()
                .productPackageId(productPackageId)
                .uniqueUserCount(count)
                .build();
    }
}
