package com.aspire.asat.registration.service.support;

import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.cms.response.CmsFullProductResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Resolves which CMS product IDs are simulation products (Phishing / Smishing / Vishing)
 * by matching configured product names against the CMS catalog.
 */
@Component
@Slf4j
public class SimulationProductResolver {

    private final CmsServiceClient cmsServiceClient;
    private final Set<String> simulationProductNames;
    private final ConcurrentHashMap<String, Boolean> productIdCache = new ConcurrentHashMap<>();

    public SimulationProductResolver(
            CmsServiceClient cmsServiceClient,
            @Value("${license.simulation-product-names:Phishing Simulation,Smishing Simulation,Vishing Simulation}")
            List<String> simulationProductNames) {
        this.cmsServiceClient = cmsServiceClient;
        this.simulationProductNames = simulationProductNames == null
                ? Set.of()
                : simulationProductNames.stream()
                .filter(StringUtils::hasText)
                .map(name -> name.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isSimulationProduct(String productId) {
        if (!StringUtils.hasText(productId) || simulationProductNames.isEmpty()) {
            return false;
        }
        String key = productId.trim();
        return productIdCache.computeIfAbsent(key, this::resolveIsSimulationProduct);
    }

    /**
     * Product IDs from the CMS catalog that match configured simulation product names.
     */
    public Set<String> getSimulationProductIds() {
        if (simulationProductNames.isEmpty()) {
            return Collections.emptySet();
        }
        Map<String, CmsFullProductResponseDto> products = cmsServiceClient.fetchAndCacheAllProducts();
        if (products == null || products.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> ids = new HashSet<>();
        for (CmsFullProductResponseDto product : products.values()) {
            if (product == null || !StringUtils.hasText(product.getProductId())) {
                continue;
            }
            if (matchesSimulationName(product.getProductName())) {
                ids.add(product.getProductId());
                productIdCache.put(product.getProductId(), true);
            }
        }
        return ids;
    }

    private boolean resolveIsSimulationProduct(String productId) {
        Map<String, CmsFullProductResponseDto> products = cmsServiceClient.fetchAndCacheAllProducts();
        if (products != null) {
            CmsFullProductResponseDto product = products.get(productId);
            if (product != null) {
                return matchesSimulationName(product.getProductName());
            }
        }
        // Fallback: single-product fetch if catalog miss
        try {
            var details = cmsServiceClient.getProductWithPackages(productId);
            if (details != null) {
                return matchesSimulationName(details.getProductName());
            }
        } catch (Exception e) {
            log.warn("Unable to resolve product name for productId={}", productId, e);
        }
        return false;
    }

    private boolean matchesSimulationName(String productName) {
        if (!StringUtils.hasText(productName)) {
            return false;
        }
        return simulationProductNames.contains(productName.trim().toLowerCase(Locale.ROOT));
    }
}
