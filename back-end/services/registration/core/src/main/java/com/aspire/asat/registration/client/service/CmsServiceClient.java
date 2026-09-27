package com.aspire.asat.registration.client.service;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.cms.PackageRangePricingResponseDto;
import com.aspire.asat.registration.data.cms.request.CmsTopicsByProductPackagesRequestDto;
import com.aspire.asat.registration.data.cms.request.CmsTopicCountsByProductPackagesRequestDto;
import com.aspire.asat.registration.data.cms.request.TrialSubPackageCreationRequestDto;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageDto;
import com.aspire.asat.registration.data.cms.response.CmsPackageSimpleResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsProductPageResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsProductResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsProductWithSinglePackageResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsTopicPageResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsTopicCountsByProductPackagesResponseDto;
import com.aspire.asat.registration.data.cms.response.CmsTopicDistributionItemDto;
import com.aspire.asat.registration.data.cms.response.CmsTrainingCertificateCountResponseDto;
import com.aspire.asat.registration.data.cms.response.SubPackageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsServiceClient {

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    private final WebClient webClient;

    // Cache for product details to improve performance
    private final ConcurrentMap<String, CmsProductResponseDto> productCache = new ConcurrentHashMap<>();

    // Cache for full product details with packages
    private final Map<String, CmsFullProductResponseDto> fullProductCache = new HashMap<>();


    /**
     * Fetch all products from CMS service and cache them
     * Populates fullProductCache with productId as key
     */
    public Map<String, CmsFullProductResponseDto> fetchAndCacheAllProducts() {
        try {
            log.info("Fetching all products from CMS service to populate fullProductCache");

            String url = cmsServiceUrl + "/products?offset=0&pageSize=1000&sortBy=createdAt&order=desc";

            ApiResponseDto<CmsProductPageResponseDto> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<CmsProductPageResponseDto>>() {
                    })
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null && response.getData() != null && response.getData().getItems() != null) {
                fullProductCache.clear();
                for (CmsFullProductResponseDto product : response.getData().getItems()) {
                    if (product.getProductId() != null) {
                        fullProductCache.put(product.getProductId(), product);
                    }
                }
                log.info("Successfully cached {} products in fullProductCache", fullProductCache.size());
            } else {
                log.warn("No products found in CMS service response");
            }

        } catch (WebClientResponseException e) {
            log.error("Error fetching all products from CMS service. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error fetching all products from CMS service", e);
        }

        return fullProductCache;
    }


    /**
     * Get full product from cache by productId
     *
     * @param productId The product ID
     * @return Full product details or null if not found
     */
    public CmsFullProductResponseDto getFullProductFromCache(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            log.warn("Product ID is null or empty");
            return null;
        }

        // If cache is empty, fetch and populate it
        if (fullProductCache.isEmpty()) {
            fetchAndCacheAllProducts();
        }

        return fullProductCache.get(productId);
    }

    /**
     * Clear the full product cache
     */
    public void clearFullProductCache() {
        log.info("Clearing full product cache");
        fullProductCache.clear();
    }

    /**
     * Get full product cache size for monitoring
     *
     * @return Current full product cache size
     */
    public int getFullProductCacheSize() {
        return fullProductCache.size();
    }

    /**
     * Fetch product details from CMS service (without packages array)
     * @param productId The product ID to fetch
     * @return Product details
     */
    public CmsProductResponseDto getProductWithPackages(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            log.warn("Product ID is null or empty");
            return null;
        }

        // Check cache first
        CmsProductResponseDto cachedProduct = productCache.get(productId);
        if (cachedProduct != null) {
            log.debug("Returning cached product details for productId: {}", productId);
            return cachedProduct;
        }

        try {
            log.info("Fetching product details from CMS service for productId: {}", productId);

            String url = cmsServiceUrl + "/products/" + productId;

            ApiResponseDto<CmsFullProductResponseDto> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<CmsFullProductResponseDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getData() != null) {
                // Get the full response from CMS service
                CmsFullProductResponseDto fullResponse = response.getData();

                // Create simplified product DTO without packages array
                CmsProductResponseDto productDetails = CmsProductResponseDto.builder()
                        .productId(fullResponse.getProductId())
                        .productName(fullResponse.getProductName())
                        .productDescription(fullResponse.getProductDescription())
                        .productStatus(fullResponse.getProductStatus())
                        .thumbnailUrl(fullResponse.getThumbnailUrl())
                        .displayOrder(fullResponse.getDisplayOrder())
                        .build();

                // Cache the result
                productCache.put(productId, productDetails);

                log.info("Successfully fetched product details for productId: {}, productName: {}",
                        productId, productDetails.getProductName());
                return productDetails;
            } else {
                log.warn("No product details found for productId: {}", productId);
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("Error fetching product details from CMS service for productId: {}. Status: {}, Response: {}",
                    productId, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching product details from CMS service for productId: {}", productId, e);
            return null;
        }
    }

    /**
     * Get product with single package details and topic count from CMS service
     * This replaces the need for two separate calls (getProductWithPackages + getPackageDetails)
     * @param productId The product ID
     * @param packageId The package ID
     * @return Product with single package and topic count, or null if not found
     */
    public CmsProductWithSinglePackageResponseDto getProductPackageDetails(String productId, String packageId) {
        if (productId == null || packageId == null || productId.trim().isEmpty() || packageId.trim().isEmpty()) {
            log.warn("Product ID or Package ID is null or empty");
            return null;
        }

        try {
            log.info("Fetching product with package details from CMS service for productId: {}, packageId: {}", productId, packageId);

            String url = cmsServiceUrl + "/products/package?productId=" + productId + "&packageId=" + packageId;

            ApiResponseDto<CmsProductWithSinglePackageResponseDto> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<CmsProductWithSinglePackageResponseDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getData() != null) {
                log.info("Successfully fetched product with package details for productId: {}, packageId: {}, topicCount: {}",
                        productId, packageId, response.getData().getTopicCount());
                return response.getData();
            } else {
                log.warn("No product package details found for productId: {}, packageId: {}", productId, packageId);
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("Error fetching product package details from CMS service for productId: {}, packageId: {}. Status: {}, Response: {}",
                    productId, packageId, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching product package details from CMS service for productId: {}, packageId: {}", productId, packageId, e);
            return null;
        }
    }

    /**
     * Get specific package details for a product
     * @param productId The product ID
     * @param packageId The package ID to find
     * @return Package details or null if not found
     * @deprecated Use getProductPackageDetails instead to avoid redundant API calls
     */
    @Deprecated
    public CmsPackageDto getPackageDetails(String productId, String packageId) {
        if (productId == null || packageId == null || productId.trim().isEmpty() || packageId.trim().isEmpty()) {
            log.warn("Product ID or Package ID is null or empty");
            return null;
        }

        try {
            log.debug("Fetching package details for productId: {}, packageId: {}", productId, packageId);

            String url = cmsServiceUrl + "/products/" + productId;

            ApiResponseDto<CmsFullProductResponseDto> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponseDto<CmsFullProductResponseDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getData() != null && response.getData().getPackages() != null) {
                // Find the specific package
                CmsPackageDto packageDetails = response.getData().getPackages().stream()
                        .filter(pkg -> pkg.getId() != null && pkg.getId().equals(packageId))
                        .findFirst()
                        .orElse(null);

                if (packageDetails != null) {
                    log.debug("Found package details for packageId: {}, packageName: {}",
                            packageId, packageDetails.getPackageName());
                } else {
                    log.warn("Package not found for productId: {}, packageId: {}", productId, packageId);
                }

                return packageDetails;
            } else {
                log.warn("No packages found for productId: {}", productId);
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("Error fetching package details from CMS service for productId: {}, packageId: {}. Status: {}, Response: {}",
                    productId, packageId, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching package details from CMS service for productId: {}, packageId: {}", productId, packageId, e);
            return null;
        }
    }

    /**
     * Clear the product cache
     */
    public void clearCache() {
        log.info("Clearing product cache");
        productCache.clear();
    }

    /**
     * Remove specific product from cache
     * @param productId The product ID to remove from cache
     */
    public void evictFromCache(String productId) {
        if (productId != null) {
            log.info("Evicting product from cache: {}", productId);
            productCache.remove(productId);
        }
    }

    /**
     * Get cache size for monitoring
     * @return Current cache size
     */
    public int getCacheSize() {
        return productCache.size();
    }

    /**
     * Fetch product packages by IDs from CMS (minimal id + name).
     *
     * @param packageIds package IDs to resolve
     * @return matching packages, or empty list on failure / empty input
     */
    public List<CmsPackageSimpleResponseDto> getPackagesByIds(Collection<String> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return List.of();
        }

        List<String> ids = packageIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        try {
            log.info("Fetching {} packages by IDs from CMS service", ids.size());

            String url = cmsServiceUrl + "/products/packages/by-ids";
            Map<String, Object> body = Map.of("ids", ids);

            ApiResponseDto<List<CmsPackageSimpleResponseDto>> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<List<CmsPackageSimpleResponseDto>>>() {})
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response != null && response.getData() != null) {
                log.info("Successfully fetched {} packages from CMS by IDs", response.getData().size());
                return response.getData();
            }

            log.warn("No packages returned from CMS for {} requested IDs", ids.size());
            return List.of();
        } catch (WebClientResponseException e) {
            log.error("Error fetching packages by IDs from CMS. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error fetching packages by IDs from CMS", e);
            return List.of();
        }
    }

    /**
     * Get package range pricing by packageId and userRangeId from CMS service
     *
     * @param packageId   The package ID
     * @param userRangeId The user range ID
     * @return PackageRangePricingResponseDto or null if not found
     */
    public PackageRangePricingResponseDto getPackageRangePricing(String packageId, String userRangeId) {
        if (packageId == null || packageId.trim().isEmpty()) {
            log.warn("Package ID is null or empty");
            return null;
        }

        try {
            log.info("Fetching package range pricing from CMS service for packageId: {}, userRangeId: {}", packageId, userRangeId);

            String url = cmsServiceUrl + "/package-range-pricing/package/" + packageId + "/range?rangeId=" + userRangeId;

            ApiResponseDto<PackageRangePricingResponseDto> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<PackageRangePricingResponseDto>>() {})
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getData() != null) {
                log.info("Successfully fetched package range pricing for packageId: {}, userRangeId: {}, pricePerUser: {}",
                        packageId, userRangeId, response.getData().getPricePerUser());
                return response.getData();
            } else {
                log.warn("No package range pricing found for packageId: {}, userRangeId: {}", packageId, userRangeId);
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("Error fetching package range pricing from CMS service for packageId: {}, userRangeId: {}. Status: {}, Response: {}",
                    packageId, userRangeId, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching package range pricing from CMS service for packageId: {}, userRangeId: {}",
                    packageId, userRangeId, e);
            return null;
        }
    }

    /**
     * Create a trial subPackage in CMS service
     * @param request The trial subPackage creation request
     * @return Created subPackage response with ID, or null if creation failed
     */
    public SubPackageResponseDto createTrialSubPackage(TrialSubPackageCreationRequestDto request) {
        if (request == null) {
            log.warn("Trial subPackage request is null");
            return null;
        }

        try {
            log.info("Creating trial subPackage in CMS service for productId: {}, packageId: {}, clientAdminId: {}, trialPeriodDays: {}",
                    request.getProductId(), request.getPackageId(), request.getClientAdminId(), request.getTrialPeriodDays());

            String url = cmsServiceUrl + "/sub-packages/trial";

            ApiResponseDto<SubPackageResponseDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<SubPackageResponseDto>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null && response.getData() != null) {
                log.info("Successfully created trial subPackage with ID: {}", response.getData().getId());
                return response.getData();
            } else {
                log.warn("Trial subPackage creation returned null response");
                return null;
            }

        } catch (WebClientResponseException e) {
            log.error("Error creating trial subPackage in CMS service. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error creating trial subPackage in CMS service", e);
            return null;
        }
    }

    /**
     * Check if certificate template is configured for a client
     * @param clientId The client admin ID to check
     * @return true if certificate template exists, false otherwise or on error
     */
    public boolean checkCertificateTemplateExists(String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            log.warn("ClientId is null or empty, returning false");
            return false;
        }

        try {
            log.info("Checking certificate template existence for clientId: {}", clientId);

            String url = cmsServiceUrl + "/certificate-templates/check-exists?clientId=" + clientId.trim();

            ApiResponseDto<Boolean> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<Boolean>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null && response.getData() != null) {
                log.info("Certificate template exists for clientId {}: {}", clientId, response.getData());
                return response.getData();
            } else {
                log.warn("Certificate template check returned null response for clientId: {}", clientId);
                return false;
            }

        } catch (WebClientResponseException e) {
            log.error("Error checking certificate template existence in CMS service. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error checking certificate template existence in CMS service for clientId: {}", clientId, e);
            return false;
        }
    }

    /**
     * Fetch training-completed and certificate-earned counts from CMS for a client admin.
     * Returns zeros when the CMS call fails so the onboarding report can still be returned.
     */
    public CmsTrainingCertificateCountResponseDto fetchTrainingCertificateCounts(String clientAdminId) {
        if (clientAdminId == null || clientAdminId.trim().isEmpty()) {
            log.warn("clientAdminId is null or empty, returning zero training/certificate counts");
            return CmsTrainingCertificateCountResponseDto.builder()
                    .trainingCompletedCount(0)
                    .certificateEarnedCount(0)
                    .build();
        }

        try {
            log.info("Fetching training/certificate counts from CMS for clientAdminId={}", clientAdminId);

            String url = cmsServiceUrl + "/reports/training-certificate-counts?clientAdminId=" + clientAdminId.trim();

            ApiResponseDto<CmsTrainingCertificateCountResponseDto> response = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsTrainingCertificateCountResponseDto>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null && response.getData() != null) {
                log.info("CMS training/certificate counts for clientAdminId {}: completed={}, certificates={}",
                        clientAdminId,
                        response.getData().getTrainingCompletedCount(),
                        response.getData().getCertificateEarnedCount());
                return response.getData();
            }

            log.warn("CMS training/certificate count response was null for clientAdminId={}", clientAdminId);
            return CmsTrainingCertificateCountResponseDto.builder()
                    .trainingCompletedCount(0)
                    .certificateEarnedCount(0)
                    .build();

        } catch (WebClientResponseException e) {
            log.error("Error fetching training/certificate counts from CMS. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return CmsTrainingCertificateCountResponseDto.builder()
                    .trainingCompletedCount(0)
                    .certificateEarnedCount(0)
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error fetching training/certificate counts from CMS for clientAdminId={}",
                    clientAdminId, e);
            return CmsTrainingCertificateCountResponseDto.builder()
                    .trainingCompletedCount(0)
                    .certificateEarnedCount(0)
                    .build();
        }
    }

    /**
     * Fetch ENABLED topics from CMS for a list of productId/packageId pairs.
     */
    public CmsTopicPageResponseDto fetchTopicsByProductPackages(CmsTopicsByProductPackagesRequestDto request) {
        if (request == null) {
            log.warn("Topics by product packages request is null");
            return CmsTopicPageResponseDto.builder()
                    .offset(0)
                    .pageSize(10)
                    .total(0)
                    .totalLocked(0)
                    .items(java.util.List.of())
                    .build();
        }

        try {
            int pairCount = request.getProductPackages() != null ? request.getProductPackages().size() : 0;
            log.info("Fetching topics from CMS for {} product/package pairs (excludeMatching={})",
                    pairCount, request.getExcludeMatchingPairs());

            String url = cmsServiceUrl + "/topics/by-product-packages";

            ApiResponseDto<CmsTopicPageResponseDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsTopicPageResponseDto>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null && response.getData() != null) {
                log.info("Successfully fetched {} topics from CMS (total={}, totalLocked={})",
                        response.getData().getItems() != null ? response.getData().getItems().size() : 0,
                        response.getData().getTotal(),
                        response.getData().getTotalLocked());
                return response.getData();
            }

            log.warn("No topics returned from CMS for product/package pairs");
            return CmsTopicPageResponseDto.builder()
                    .offset(request.getOffset() != null ? request.getOffset() : 0)
                    .pageSize(request.getPageSize() != null ? request.getPageSize() : 10)
                    .total(0)
                    .totalLocked(0)
                    .items(java.util.List.of())
                    .build();

        } catch (WebClientResponseException e) {
            log.error("Error fetching topics by product/package pairs from CMS. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return CmsTopicPageResponseDto.builder()
                    .offset(0)
                    .pageSize(10)
                    .total(0)
                    .totalLocked(0)
                    .items(java.util.List.of())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error fetching topics by product/package pairs from CMS", e);
            return CmsTopicPageResponseDto.builder()
                    .offset(0)
                    .pageSize(10)
                    .total(0)
                    .totalLocked(0)
                    .items(java.util.List.of())
                    .build();
        }
    }

    /**
     * Count ENABLED topics in CMS for MSP and client product/package pair lists,
     * returned as monthly distribution for the current year.
     */
    public CmsTopicCountsByProductPackagesResponseDto countTopicsByProductPackages(
            CmsTopicCountsByProductPackagesRequestDto request) {
        if (request == null) {
            log.warn("Topic counts by product packages request is null");
            return emptyTopicDistribution();
        }

        try {
            int mspPairs = request.getMspProductPackages() != null ? request.getMspProductPackages().size() : 0;
            int clientPairs = request.getClientProductPackages() != null
                    ? request.getClientProductPackages().size() : 0;
            log.info("Counting monthly topics from CMS for mspPairs={}, clientPairs={}", mspPairs, clientPairs);

            String url = cmsServiceUrl + "/topics/counts-by-product-packages";

            ApiResponseDto<CmsTopicCountsByProductPackagesResponseDto> response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<CmsTopicCountsByProductPackagesResponseDto>>() {})
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null && response.getData() != null) {
                int months = response.getData().getData() != null ? response.getData().getData().size() : 0;
                log.info("Successfully fetched monthly topic distribution from CMS ({} months)", months);
                return response.getData();
            }

            log.warn("No topic distribution returned from CMS");
            return emptyTopicDistribution();

        } catch (WebClientResponseException e) {
            log.error("Error counting topics by product/package pairs from CMS. Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return emptyTopicDistribution();
        } catch (Exception e) {
            log.error("Unexpected error counting topics by product/package pairs from CMS", e);
            return emptyTopicDistribution();
        }
    }

    private CmsTopicCountsByProductPackagesResponseDto emptyTopicDistribution() {
        String[] monthNames = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        List<CmsTopicDistributionItemDto> data = java.util.stream.IntStream.range(0, 12)
                .mapToObj(i -> CmsTopicDistributionItemDto.builder()
                        .month(monthNames[i])
                        .totalContent(0L)
                        .usedContent(0L)
                        .build())
                .toList();
        return CmsTopicCountsByProductPackagesResponseDto.builder().data(data).build();
    }
}
