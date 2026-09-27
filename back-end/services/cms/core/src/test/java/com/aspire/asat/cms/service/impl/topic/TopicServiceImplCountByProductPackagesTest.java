package com.aspire.asat.cms.service.impl.topic;

import com.aspire.asat.cms.dto.topic.ProductPackagePairDto;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesRequest;
import com.aspire.asat.cms.dto.topic.TopicCountsByProductPackagesResponseDto;
import com.aspire.asat.cms.repository.topic.TopicFilterRepositoryCustom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicServiceImplCountByProductPackagesTest {

    @Mock
    private TopicFilterRepositoryCustom topicFilterRepositoryCustom;

    @InjectMocks
    private TopicServiceImpl topicService;

    @Test
    void countTopicsByProductPackages_throwsWhenRequestNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicService.countTopicsByProductPackages(null));
    }

    @Test
    void countTopicsByProductPackages_returnsMonthlyDistribution() {
        List<ProductPackagePairDto> mspPairs = List.of(
                ProductPackagePairDto.builder().productId("p1").packageId("pkg1").build());
        List<ProductPackagePairDto> clientPairs = List.of(
                ProductPackagePairDto.builder().productId("p2").packageId("pkg2").build());

        when(topicFilterRepositoryCustom.countTopicsByProductPackagePairsByMonth(
                eq(mspPairs), any(Instant.class), any(Instant.class)))
                .thenReturn(Map.of(4, 33L, 7, 2L));
        when(topicFilterRepositoryCustom.countTopicsByProductPackagePairsByMonth(
                eq(clientPairs), any(Instant.class), any(Instant.class)))
                .thenReturn(Map.of(4, 32L, 5, 32L, 7, 20L));

        TopicCountsByProductPackagesResponseDto response = topicService.countTopicsByProductPackages(
                TopicCountsByProductPackagesRequest.builder()
                        .mspProductPackages(mspPairs)
                        .clientProductPackages(clientPairs)
                        .build());

        assertEquals(12, response.getData().size());
        assertEquals("Jan", response.getData().get(0).getMonth());
        assertEquals(0L, response.getData().get(0).getTotalContent());
        assertEquals(0L, response.getData().get(0).getUsedContent());

        assertEquals("Apr", response.getData().get(3).getMonth());
        assertEquals(33L, response.getData().get(3).getTotalContent());
        assertEquals(32L, response.getData().get(3).getUsedContent());

        assertEquals("May", response.getData().get(4).getMonth());
        assertEquals(0L, response.getData().get(4).getTotalContent());
        assertEquals(32L, response.getData().get(4).getUsedContent());
    }
}
