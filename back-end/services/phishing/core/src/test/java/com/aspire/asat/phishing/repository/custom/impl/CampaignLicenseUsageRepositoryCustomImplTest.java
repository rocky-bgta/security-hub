package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.response.CampaignLicenseUsageDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignLicenseUsageRepositoryCustomImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CampaignLicenseUsageRepositoryCustomImpl repository;

    @Test
    void countUniqueUsersByProductPackageId_blankClient_returnsEmptyWithoutQuery() {
        List<CampaignLicenseUsageDto> result =
                repository.countUniqueUsersByProductPackageId("  ", "pp-1");

        assertTrue(result.isEmpty());
        verify(mongoTemplate, never()).aggregate(any(Aggregation.class), any(String.class), any(Class.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void countUniqueUsersByProductPackageId_returnsMappedResults() {
        CampaignLicenseUsageDto row = CampaignLicenseUsageDto.builder()
                .productPackageId("pp-1")
                .uniqueUserCount(10)
                .build();
        AggregationResults<CampaignLicenseUsageDto> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(List.of(row));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("phishing_user_licence"), eq(CampaignLicenseUsageDto.class)))
                .thenReturn(results);

        List<CampaignLicenseUsageDto> usage =
                repository.countUniqueUsersByProductPackageId("client-1", "pp-1");

        assertEquals(1, usage.size());
        assertEquals(10, usage.get(0).getUniqueUserCount());
        assertEquals("pp-1", usage.get(0).getProductPackageId());

        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongoTemplate).aggregate(captor.capture(), eq("phishing_user_licence"), eq(CampaignLicenseUsageDto.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void countUniqueUsersByProductPackageId_withoutFilter_aggregatesAllAssignments() {
        AggregationResults<CampaignLicenseUsageDto> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(List.of());
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("phishing_user_licence"), eq(CampaignLicenseUsageDto.class)))
                .thenReturn(results);

        List<CampaignLicenseUsageDto> usage =
                repository.countUniqueUsersByProductPackageId("client-1", null);

        assertTrue(usage.isEmpty());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("phishing_user_licence"), eq(CampaignLicenseUsageDto.class));
    }
}
