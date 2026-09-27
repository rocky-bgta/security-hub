package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.client.ClientPackageSimpleResponse;
import com.aspire.asat.cms.dto.client.ClientProductSimpleResponse;
import com.aspire.asat.cms.model.ClientProductReplica;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.ProductPackage;
import com.aspire.asat.cms.repository.ClientProductReplicaRepository;
import com.aspire.asat.cms.repository.ProductPackageRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.service.ClientProductService;
import com.aspire.asat.common.service.files.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for client product operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientProductServiceImpl implements ClientProductService {

    private final ClientProductReplicaRepository clientProductReplicaRepository;
    private final ProductRepository productRepository;
    private final ProductPackageRepository productPackageRepository;
    private final FileService fileService;

    @Override
    public List<ClientProductSimpleResponse> getUniqueProductsByClientAdminId(String clientAdminId) {
        log.info("Fetching unique products for clientAdminId: {}", clientAdminId);

        // Step 1: Get all ClientProductReplica records for this clientAdminId
        List<ClientProductReplica> replicas = clientProductReplicaRepository.findByClientAdminId(clientAdminId);
        
        if (replicas.isEmpty()) {
            log.info("No product replicas found for clientAdminId: {}", clientAdminId);
            return List.of();
        }

        // Step 2: Extract unique productIds from replicas
        Set<String> uniqueProductIds = replicas.stream()
                .map(ClientProductReplica::getProductId)
                .filter(productId -> productId != null && !productId.trim().isEmpty())
                .collect(Collectors.toSet());

        if (uniqueProductIds.isEmpty()) {
            log.info("No valid productIds found in replicas for clientAdminId: {}", clientAdminId);
            return List.of();
        }

        log.info("Found {} unique productIds for clientAdminId: {}", uniqueProductIds.size(), clientAdminId);

        // Step 3: Fetch products from Product collection by productIds
        List<Product> products = productRepository.findAllById(uniqueProductIds);

        if (products.isEmpty()) {
            log.warn("No products found for productIds: {} for clientAdminId: {}", uniqueProductIds, clientAdminId);
            return List.of();
        }

        log.info("Found {} products for clientAdminId: {}", products.size(), clientAdminId);

        // Step 4: Sort by displayOrder ascending (nulls last)
        products.sort(Comparator.comparing(Product::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())));

        // Step 5: Map to response DTOs with file service path resolution
        List<ClientProductSimpleResponse> response = products.stream()
                .map(product -> ClientProductSimpleResponse.builder()
                        .id(product.getId())
                        .productName(product.getProductName())
                        .thumbnailUrl(fileService.getPath(product.getThumbnailUrl()))
                        .displayOrder(product.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        log.info("Successfully mapped {} products to response for clientAdminId: {}", response.size(), clientAdminId);

        return response;
    }

    @Override
    public List<ClientPackageSimpleResponse> getPackagesByClientAdminIdAndProductId(String clientAdminId, String productId) {
        log.info("Fetching packages for clientAdminId: {} and productId: {}", clientAdminId, productId);

        // Step 1: Get all ClientProductReplica records for this clientAdminId and productId
        List<ClientProductReplica> replicas = clientProductReplicaRepository.findByClientAdminIdAndProductId(clientAdminId, productId);
        
        if (replicas.isEmpty()) {
            log.info("No product replicas found for clientAdminId: {} and productId: {}", clientAdminId, productId);
            return List.of();
        }

        // Step 2: Extract unique packageIds from replicas
        Set<String> uniquePackageIds = replicas.stream()
                .map(ClientProductReplica::getPackageId)
                .filter(packageId -> packageId != null && !packageId.trim().isEmpty())
                .collect(Collectors.toSet());

        if (uniquePackageIds.isEmpty()) {
            log.info("No valid packageIds found in replicas for clientAdminId: {} and productId: {}", clientAdminId, productId);
            return List.of();
        }

        log.info("Found {} unique packageIds for clientAdminId: {} and productId: {}", uniquePackageIds.size(), clientAdminId, productId);

        // Step 3: Fetch packages from ProductPackage collection by packageIds
        List<ProductPackage> packages = productPackageRepository.findAllById(uniquePackageIds);

        if (packages.isEmpty()) {
            log.warn("No packages found for packageIds: {} for clientAdminId: {} and productId: {}", uniquePackageIds, clientAdminId, productId);
            return List.of();
        }

        log.info("Found {} packages for clientAdminId: {} and productId: {}", packages.size(), clientAdminId, productId);

        // Step 4: Map to response DTOs
        List<ClientPackageSimpleResponse> response = packages.stream()
                .map(pkg -> ClientPackageSimpleResponse.builder()
                        .id(pkg.getId())
                        .name(pkg.getName())
                        .build())
                .collect(Collectors.toList());

        log.info("Successfully mapped {} packages to response for clientAdminId: {} and productId: {}", response.size(), clientAdminId, productId);

        return response;
    }
}

