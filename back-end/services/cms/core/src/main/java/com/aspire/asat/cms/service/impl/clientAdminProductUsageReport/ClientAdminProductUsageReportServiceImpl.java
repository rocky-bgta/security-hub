package com.aspire.asat.cms.service.impl.clientAdminProductUsageReport;

import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.clientAdminProductUsageReport.ClientAdminProductUsageReportProductDto;
import com.aspire.asat.cms.dto.clientAdminProductUsageReport.ClientAdminProductUsageReportResponseDto;
import com.aspire.asat.cms.dto.registration.RegistrationClientProductDto;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.topic.TopicFilterRepositoryCustom;
import com.aspire.asat.cms.service.clientAdminProductUsageReport.ClientAdminProductUsageReportService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.cms.util.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientAdminProductUsageReportServiceImpl implements ClientAdminProductUsageReportService {

    private final ClientAdminServiceClient clientAdminServiceClient;
    private final TopicFilterRepositoryCustom topicFilterRepositoryCustom;
    private final ProductRepository productRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ClientAdminProductUsageReportResponseDto getClientAdminProductUsageReport(String clientAdminId) {
        String resolvedClientAdminId = resolveClientAdminId(clientAdminId);
        log.info("Building client admin product usage report for clientAdminId: {}", resolvedClientAdminId);

        Instant now = Instant.now();
        List<RegistrationClientProductDto> activeProducts = getValidClientProducts(resolvedClientAdminId, now);

        int totalProduct = (int) activeProducts.stream()
                .map(RegistrationClientProductDto::getProductId)
                .filter(StringUtils::hasText)
                .distinct()
                .count();

        Set<String> productIds = activeProducts.stream()
                .map(RegistrationClientProductDto::getProductId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> packageIds = activeProducts.stream()
                .map(RegistrationClientProductDto::getPackageId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int totalActiveModule = countUniqueTopics(productIds, packageIds);
        int totalInteraction = activeProducts.stream().mapToInt(RegistrationClientProductDto::getLicenseCount).sum();
        int totalUsedLicenses = activeProducts.stream().mapToInt(RegistrationClientProductDto::getUsedLicenseCount).sum();
        double averageEngagement = totalInteraction > 0
                ? roundPercentage((double) totalUsedLicenses / totalInteraction * 100.0)
                : 0.0;

        Map<String, String> productNameByProductId = fetchProductNamesByProductId(productIds);

        List<ClientAdminProductUsageReportProductDto> products = activeProducts.stream()
                .map(product -> toProductUsageDto(product, productNameByProductId))
                .toList();

        log.info("Client admin product usage report for {}: products={}, modules={}, interaction={}, engagement={}%",
                resolvedClientAdminId, totalProduct, totalActiveModule, totalInteraction, averageEngagement);

        return ClientAdminProductUsageReportResponseDto.builder()
                .clientAdminId(resolvedClientAdminId)
                .totalProduct(totalProduct)
                .totalActiveModule(totalActiveModule)
                .totalInteraction(totalInteraction)
                .averageEngagement(averageEngagement)
                .products(products)
                .build();
    }

    @Override
    public byte[] exportClientAdminProductUsageReportCsv(String clientAdminId) {
        ClientAdminProductUsageReportResponseDto report = getClientAdminProductUsageReport(clientAdminId);
        return buildProductsCsv(report.getProducts());
    }

    String resolveClientAdminId(String clientAdminId) {
        if (StringUtils.hasText(clientAdminId)) {
            return clientAdminId.trim();
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (UserType.CLIENT_ADMIN.name().equals(userContext.getUserType())) {
            return userContext.getUserId();
        }

        throw new ResourceNotFoundException("clientAdminId is required when the current user is not a CLIENT_ADMIN");
    }

    private List<RegistrationClientProductDto> getValidClientProducts(String clientAdminId, Instant currentTime) {
        return clientAdminServiceClient.getClientProductsByClientAdminId(clientAdminId).stream()
                .filter(product -> product.getExpiryDate() != null)
                .toList();
    }

    private int countUniqueTopics(Set<String> productIds, Set<String> packageIds) {
        if (productIds.isEmpty() && packageIds.isEmpty()) {
            return 0;
        }

        Long count = topicFilterRepositoryCustom.countUniqueTopicsByProductAndPackage(
                new ArrayList<>(productIds),
                new ArrayList<>(packageIds));
        return count != null ? count.intValue() : 0;
    }

    private Map<String, String> fetchProductNamesByProductId(Set<String> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        return productRepository.findAllById(productIds).stream()
                .filter(product -> StringUtils.hasText(product.getId()))
                .collect(Collectors.toMap(
                        Product::getId,
                        product -> StringUtils.hasText(product.getProductName())
                                ? product.getProductName()
                                : product.getId(),
                        (existing, replacement) -> existing
                ));
    }

    private ClientAdminProductUsageReportProductDto toProductUsageDto(
            RegistrationClientProductDto product,
            Map<String, String> productNameByProductId) {

        int totalLicenseCount = product.getLicenseCount();
        int totalUsers = product.getUsedLicenseCount();
        double utilizationPercentage = totalLicenseCount > 0
                ? roundPercentage((double) totalUsers / totalLicenseCount * 100.0)
                : 0.0;

        String productName = productNameByProductId.get(product.getProductId());

        return ClientAdminProductUsageReportProductDto.builder()
                .id(product.getId())
                .productId(product.getProductId())
                .packageId(product.getPackageId())
                .productName(productName)
                .totalUsers(totalUsers)
                .totalLicenseCount(totalLicenseCount)
                .utilizationPercentage(utilizationPercentage)
                .build();
    }

    private double roundPercentage(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static byte[] buildProductsCsv(List<ClientAdminProductUsageReportProductDto> products) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("productName,totalUsers,totalLicenseCount,utilizationPercentage\n");
        if (products != null) {
            for (ClientAdminProductUsageReportProductDto product : products) {
                sb.append(escapeCsv(product.getProductName())).append(',');
                sb.append(product.getTotalUsers()).append(',');
                sb.append(product.getTotalLicenseCount()).append(',');
                sb.append(product.getUtilizationPercentage()).append('\n');
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
