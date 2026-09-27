package com.aspire.asat.cms.service.impl.dashboard;

import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.dashboard.ProductTopicSalesProgressResponseDto;
import com.aspire.asat.cms.dto.enums.TimeFrame;
import com.aspire.asat.cms.exception.CmsServiceException;
import com.aspire.asat.cms.model.ClientProductReplica;
import com.aspire.asat.cms.repository.ClientProductReplicaRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductTopicSalesProgressServiceImplTest {

    private static final String MSP_ID = "msp-123";

    @Mock private MongoTemplate mongoTemplate;
    @Mock private ClientProductReplicaRepository clientProductReplicaRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ClientAdminServiceClient clientAdminServiceClient;

    @InjectMocks
    private ProductTopicSalesProgressServiceImpl service;

    @Test
    void getMspProductTopicSalesProgress_throwsWhenMspIdMissing() {
        assertThrows(CmsServiceException.class,
                () -> service.getMspProductTopicSalesProgress(TimeFrame.YEARLY, null));
    }

    @Test
    void getMspProductTopicSalesProgress_returnsEmptyWhenNoClients() {
        when(clientAdminServiceClient.getClientAdminIdsByMspId(MSP_ID)).thenReturn(Collections.emptyList());

        ProductTopicSalesProgressResponseDto response =
                service.getMspProductTopicSalesProgress(TimeFrame.YEARLY, MSP_ID);

        assertNotNull(response);
        assertTrue(response.getProductCategories().isEmpty());
        assertTrue(response.getSeriesData().isEmpty());
        verify(clientProductReplicaRepository, never()).findByClientAdminIdIn(anyList());
    }

    @Test
    void getMspProductTopicSalesProgress_returnsEmptyWhenClientsHaveNoProducts() {
        when(clientAdminServiceClient.getClientAdminIdsByMspId(MSP_ID)).thenReturn(List.of("ca-1", "ca-2"));
        when(clientProductReplicaRepository.findByClientAdminIdIn(List.of("ca-1", "ca-2")))
                .thenReturn(Collections.emptyList());

        ProductTopicSalesProgressResponseDto response =
                service.getMspProductTopicSalesProgress(TimeFrame.MONTHLY, MSP_ID);

        assertNotNull(response);
        assertEquals(TimeFrame.MONTHLY.getValue(), response.getTimeFrame());
        assertTrue(response.getProductCategories().isEmpty());
        verify(clientProductReplicaRepository).findByClientAdminIdIn(List.of("ca-1", "ca-2"));
    }

    @Test
    void getMspProductTopicSalesProgress_loadsReplicasForResolvedClients() {
        when(clientAdminServiceClient.getClientAdminIdsByMspId(MSP_ID)).thenReturn(List.of("ca-1"));
        when(clientProductReplicaRepository.findByClientAdminIdIn(List.of("ca-1")))
                .thenReturn(List.of(ClientProductReplica.builder()
                        .clientAdminId("ca-1")
                        .productId(null)
                        .build()));

        ProductTopicSalesProgressResponseDto response =
                service.getMspProductTopicSalesProgress(TimeFrame.YEARLY, MSP_ID);

        assertNotNull(response);
        assertTrue(response.getProductCategories().isEmpty());
        verify(clientAdminServiceClient).getClientAdminIdsByMspId(MSP_ID);
        verify(clientProductReplicaRepository).findByClientAdminIdIn(List.of("ca-1"));
    }
}
