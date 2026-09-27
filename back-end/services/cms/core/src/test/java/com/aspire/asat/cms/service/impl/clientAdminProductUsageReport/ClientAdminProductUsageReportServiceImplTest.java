package com.aspire.asat.cms.service.impl.clientAdminProductUsageReport;

import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.registration.RegistrationClientProductDto;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.topic.TopicFilterRepositoryCustom;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientAdminProductUsageReportServiceImplTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String PACKAGE_ID = "package-1";
    private static final String PRODUCT_ID = "product-1";

    @Mock
    private ClientAdminServiceClient clientAdminServiceClient;
    @Mock
    private TopicFilterRepositoryCustom topicFilterRepositoryCustom;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private ClientAdminProductUsageReportServiceImpl clientAdminProductUsageReportService;

    @Test
    void getClientAdminProductUsageReport_usesRegistrationClientProducts() {
        Instant future = Instant.now().plus(30, ChronoUnit.DAYS);
        RegistrationClientProductDto product = RegistrationClientProductDto.builder()
                .id("cp-1")
                .clientAdminId(CLIENT_ADMIN_ID)
                .productId(PRODUCT_ID)
                .packageId(PACKAGE_ID)
                .licenseCount(100)
                .usedLicenseCount(40)
                .expiryDate(future)
                .build();

        when(clientAdminServiceClient.getClientProductsByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(List.of(product));
        when(topicFilterRepositoryCustom.countUniqueTopicsByProductAndPackage(anyList(), anyList())).thenReturn(12L);
        when(productRepository.findAllById(Set.of(PRODUCT_ID)))
                .thenReturn(List.of(Product.builder()
                        .id(PRODUCT_ID)
                        .productName("Security Awareness")
                        .build()));

        var response = clientAdminProductUsageReportService.getClientAdminProductUsageReport(CLIENT_ADMIN_ID);

        assertEquals(100, response.getTotalInteraction());
        assertEquals(40.0, response.getAverageEngagement());
        assertEquals(40, response.getProducts().get(0).getTotalUsers());
        assertEquals(100, response.getProducts().get(0).getTotalLicenseCount());
        assertEquals("Security Awareness", response.getProducts().get(0).getProductName());

        verify(clientAdminServiceClient).getClientProductsByClientAdminId(CLIENT_ADMIN_ID);
        verify(productRepository).findAllById(Set.of(PRODUCT_ID));
    }

    @Test
    void resolveClientAdminId_usesContextForClientAdmin() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN.name())
                .build());

        assertEquals(CLIENT_ADMIN_ID, clientAdminProductUsageReportService.resolveClientAdminId(null));
    }

    @Test
    void exportClientAdminProductUsageReportCsv_containsProductColumns() {
        Instant future = Instant.now().plus(30, ChronoUnit.DAYS);
        RegistrationClientProductDto product = RegistrationClientProductDto.builder()
                .id("client-product-uuid-123")
                .clientAdminId(CLIENT_ADMIN_ID)
                .productId("product-xyz-001")
                .packageId("package-abc-123")
                .licenseCount(100)
                .usedLicenseCount(40)
                .expiryDate(future)
                .build();

        when(clientAdminServiceClient.getClientProductsByClientAdminId(CLIENT_ADMIN_ID)).thenReturn(List.of(product));
        when(topicFilterRepositoryCustom.countUniqueTopicsByProductAndPackage(anyList(), anyList())).thenReturn(0L);
        when(productRepository.findAllById(Set.of("product-xyz-001")))
                .thenReturn(List.of(Product.builder()
                        .id("product-xyz-001")
                        .productName("Cybersecurity Fundamentals")
                        .build()));

        byte[] csvBytes = clientAdminProductUsageReportService.exportClientAdminProductUsageReportCsv(CLIENT_ADMIN_ID);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("productName,totalUsers,totalLicenseCount,utilizationPercentage"));
        assertTrue(csv.contains("Cybersecurity Fundamentals,40,100,40.0"));
        assertFalse(csv.contains("id,productId,packageId"));
        assertFalse(csv.contains("client-product-uuid-123"));
        assertFalse(csv.contains("product-xyz-001"));
        assertFalse(csv.contains("package-abc-123"));
    }
}
