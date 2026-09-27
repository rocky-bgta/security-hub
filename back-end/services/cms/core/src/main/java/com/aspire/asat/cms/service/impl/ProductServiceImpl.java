package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.clientAdmin.ClientProductDTO;
import com.aspire.asat.cms.dto.clientAdmin.ClientProductIdListResponseDto;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.enums.ProductStatus;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingResponse;
import com.aspire.asat.cms.dto.product.AssignedProductTagDto;
import com.aspire.asat.cms.dto.product.FeatureDto;
import com.aspire.asat.cms.dto.product.PackageRequest;
import com.aspire.asat.cms.dto.product.ProductCreationRequest;
import com.aspire.asat.cms.dto.product.ProductPackageSimpleResponse;
import com.aspire.asat.cms.dto.product.ProductResponse;
import com.aspire.asat.cms.dto.product.ProductWithSinglePackageResponse;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.mapper.ProductMapper;
import com.aspire.asat.cms.mapper.ProductPackageMapper;
import com.aspire.asat.cms.model.Feature;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.ProductPackage;
import com.aspire.asat.cms.model.ProductWithPackages;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.FeatureRepository;
import com.aspire.asat.cms.repository.ProductPackageRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.service.OrganizationDashboardService;
import com.aspire.asat.cms.service.PackageRangePricingService;
import com.aspire.asat.cms.service.ProductService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.service.files.FileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    public static final String RESOURCE_NOT_FOUND_WITH_ID = "Resource not found with id: ";
    private final ProductRepository productRepository;
    private final ProductPackageRepository productPackageRepository;
    private final ProductPackageMapper productPackageMapper;
    private final ProductMapper productMapper;
    private final FeatureRepository featureRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final FileService fileService;
    private final OrganizationDashboardService organizationDashboardService;
    private final UserCurrentContextService userCurrentContextService;
    private final PackageRangePricingService packageRangePricingService;

    @Value("${service.registration.url}")
    private String registrationUrl;


    @Override
    @Transactional
    public ProductResponse saveProduct(ProductCreationRequest requestDto) {

        checkUniqueProductName(requestDto.getProductName());

        // Create the product using mapper
        Product product = productMapper.toEntity(requestDto);

        Product savedProduct = productRepository.save(product);
        
        // Create packages if provided
        List<ProductPackage> productPackages = new ArrayList<>();
        if (!CollectionUtils.isEmpty(requestDto.getPackages())) {
            // First, validate and set isPriceRange before saving
            for (int i = 0; i < requestDto.getPackages().size(); i++) {
                PackageRequest packageRequest = requestDto.getPackages().get(i);
                
                // Validate: either price or rangePricing must be provided
                if (packageRequest.getPrice() == null && 
                    (packageRequest.getRangePricing() == null || packageRequest.getRangePricing().isEmpty())) {
                    throw new IllegalArgumentException("Either price or rangePricing must be provided for package: " + packageRequest.getPackageName());
                }

                // Set isPriceRange based on whether rangePricing is provided
                boolean hasRangePricing = !CollectionUtils.isEmpty(packageRequest.getRangePricing());
                boolean isPriceRange = packageRequest.getIsPriceRange() != null ? 
                    packageRequest.getIsPriceRange() : hasRangePricing;
                packageRequest.setIsPriceRange(isPriceRange);
            }

            // Now create packages with correct isPriceRange value
            productPackages = requestDto.getPackages().stream()
                    .map(packageRequest -> productPackageMapper.toEntity(packageRequest, savedProduct.getId()))
                    .toList();
            productPackageRepository.saveAll(productPackages);

            // Save range pricing for each package if provided
            for (int i = 0; i < productPackages.size(); i++) {
                ProductPackage pkg = productPackages.get(i);
                PackageRequest packageRequest = requestDto.getPackages().get(i);

                // Save range pricing if provided
                if (!CollectionUtils.isEmpty(packageRequest.getRangePricing())) {
                    for (PackageRangePricingRequest rangePricingRequest : packageRequest.getRangePricing()) {
                        packageRangePricingService.createPackageRangePricing(rangePricingRequest, pkg.getId());
                    }
                }
            }
        }

        // Update Organization Dashboard
        updateOrganizationDashboard(productPackages.size());

        ProductResponse response = productMapper.toCreateResponse(savedProduct);
        List<PackageRequest> packageResponses = productPackages.stream()
                .map(pkg -> {
                    PackageRequest pkgResponse = productPackageMapper.toResponse(pkg);
                    // Fetch and set range pricing
                    List<PackageRangePricingResponse> rangePricing = packageRangePricingService.getPricingByPackageId(pkg.getId());
                    pkgResponse.setRangePricingResponse(rangePricing);
                    return pkgResponse;
                })
                .toList();
        response.setPackages(packageResponses);
        return response;
    }

    /**
     * Update Organization Dashboard (Super Admin Dashboard) with product and package counts
     * This dashboard represents system-wide statistics - there is only ONE entry
     * Gets organizationAdminId from user context
     * @param packageCount number of packages created
     */
    private void updateOrganizationDashboard(int packageCount) {
        try {
            // Get user context to extract organizationAdminId
            CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
            String organizationAdminId = getOrganizationAdminId(userContext);
            String createdBy = userContext != null && userContext.getUserId() != null
                    ? userContext.getUserId() : "SYSTEM";

            //for testing
//            String organizationAdminId = "super-admin-org-001";
//            String createdBy = "super-admin-user-001";

            if (organizationAdminId != null && !organizationAdminId.trim().isEmpty()) {
                // Increment total product count (always)
                organizationDashboardService.incrementTotalProduct(organizationAdminId, createdBy);
                log.info("Updated organization dashboard - incremented totalProduct for organizationAdminId: {}", organizationAdminId);

                // Increment total package count if packages were created
                if (packageCount > 0) {
                    organizationDashboardService.incrementTotalPackage(organizationAdminId, packageCount, createdBy);
                    log.info("Updated organization dashboard - incremented totalPackage by {} for organizationAdminId: {}", 
                            packageCount, organizationAdminId);
                }
            } else {
                log.warn("Organization admin ID not found in user context, skipping dashboard update");
            }
        } catch (Exception e) {
            // Log error but don't fail the product creation
            log.error("Error updating organization dashboard: {}", e.getMessage(), e);
        }
    }

    /**
     * Get organization admin ID from user context
     * @param userContext the current user context
     * @return organization admin ID
     */
    private String getOrganizationAdminId(CurrentUserContext userContext) {
        if (userContext != null) {
            // Try to get organizationAdminId from context
            // If not available in CurrentUserContext, use userId as fallback
            // You may need to adjust this based on your actual user context structure
            // For now, using userId as organizationAdminId (assuming super admin user)
            return userContext.getUserId();
        }
        return null;
    }

    @Override
    public List<ProductResponse> getAllProducts(String search, String status, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(
                Sort.Order.asc("displayOrder").nullsLast(),
                Sort.Order.by(sortBy).with(direction)
        );
        Pageable pageable = PageRequest.of(offset, pageSize, sort);

        // Build criteria for filtering
        Criteria criteria = new Criteria();

        // Add search filter if provided
        if (!ObjectUtils.isEmpty(search)) {
            criteria.and("productName").regex(search, "i");
        }

        // Add status filter if provided
        if (!ObjectUtils.isEmpty(status)) {
            try {
                ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
                criteria.and("productStatus").is(productStatus);
            } catch (IllegalArgumentException e) {
                // If invalid status is provided, return empty list
                return new ArrayList<>();
            }
        }

        // Create query with criteria
        Query query = new Query(criteria).with(pageable);
        List<Product> products = mongoTemplate.find(query, Product.class);

        List<ProductResponse> productResponses = productMapper.toProductResponseList(products);

        // Fetch and set packages for each product
        for (ProductResponse productResponse : productResponses) {
            List<ProductPackage> productPackages = productPackageRepository.findByProductId(productResponse.getProductId());
            List<PackageRequest> packages = enrichPackagesWithRangePricing(productPackages);
            productResponse.setPackages(packages);
        }

        return productResponses;
    }

    @Override
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND_WITH_ID + id));

        ProductResponse productResponse = productMapper.toProductResponse(product);

        // Fetch and set packages for the product
        List<ProductPackage> productPackages = productPackageRepository.findByProductId(id);
        List<PackageRequest> packages = enrichPackagesWithRangePricing(productPackages);
        productResponse.setPackages(packages);
        productResponse.setThumbnailUrl(fileService.getPath(product.getThumbnailUrl()));

        return productResponse;
    }

    @Override
    public ProductWithSinglePackageResponse getProductPackageDetails(String productId, String packageId) {
        // 1. Find the product by productId
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException(RESOURCE_NOT_FOUND_WITH_ID + productId));

        // 2. Find the package by packageId
        ProductPackage productPackage = productPackageRepository.findById(packageId).orElseThrow(
                () -> new ResourceNotFoundException("Package not found with id: " + packageId));

        // 3. Verify that the package belongs to the product
        if (!productId.equals(productPackage.getProductId())) {
            throw new ResourceNotFoundException(
                    "Package with id " + packageId + " does not belong to product with id " + productId);
        }

        // 4. Map package to PackageRequest
        PackageRequest packageRequest = productPackageMapper.toResponse(productPackage);
        // Enrich with range pricing
        List<PackageRangePricingResponse> rangePricing = packageRangePricingService.getPricingByPackageId(packageId);
        packageRequest.setRangePricingResponse(rangePricing);

        // 5. Count topics for this product and package
        // Topics are linked via productPackageMappings array where:
        // - productPackageMappings.productId == productId
        // - productPackageMappings.packageIds contains packageId
        // - status == ENABLED
        Query topicQuery = new Query();
        topicQuery.addCriteria(Criteria.where("productPackageMappings").elemMatch(
                Criteria.where("productId").is(productId)
                        .and("packageIds").in(packageId)
        ));
        topicQuery.addCriteria(Criteria.where("status").is(TopicStatus.ENABLED));
        
        long topicCount = mongoTemplate.count(topicQuery, Topic.class);
        log.debug("Topic count for productId: {}, packageId: {} is {}", productId, packageId, topicCount);

        // 6. Build and return response with single package and topic count
        return ProductWithSinglePackageResponse.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productStatus(product.getProductStatus())
                .thumbnailUrl(fileService.getPath(product.getThumbnailUrl()))
                .tags(product.getTags())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .lastModifiedBy(product.getLastModifiedBy())
                .packages(packageRequest)
                .topicCount((int) topicCount)
                .displayOrder(product.getDisplayOrder())
                .build();
    }

    @Override
    public ProductResponse updateProductById(String productId, ProductCreationRequest updateRequestDto) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND_WITH_ID + productId));

        if (!updateRequestDto.getProductName().equals(existingProduct.getProductName())) {
            checkUniqueProductName(updateRequestDto.getProductName());
        }

        // Create an updated product using the same logic as create
        Product updatedProduct = productMapper.toEntity(updateRequestDto);
        updatedProduct.setId(productId);
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt());
        updatedProduct.setUpdatedAt(Instant.now());

        Product savedProduct = productRepository.save(updatedProduct);

        // Handle package updates
        List<ProductPackage> productPackages = new ArrayList<>();
        if (!CollectionUtils.isEmpty(updateRequestDto.getPackages())) {
            // Delete existing packages and their range pricing for this product
            List<ProductPackage> existingPackages = productPackageRepository.findByProductId(productId);
            for (ProductPackage existingPkg : existingPackages) {
                packageRangePricingService.deletePricingByPackageId(existingPkg.getId());
            }
            productPackageRepository.deleteByProductId(productId);

            // First, validate and set isPriceRange before saving
            for (int i = 0; i < updateRequestDto.getPackages().size(); i++) {
                PackageRequest packageRequest = updateRequestDto.getPackages().get(i);
                
                // Validate: either price or rangePricing must be provided
                if (packageRequest.getPrice() == null && 
                    (packageRequest.getRangePricing() == null || packageRequest.getRangePricing().isEmpty())) {
                    throw new IllegalArgumentException("Either price or rangePricing must be provided for package: " + packageRequest.getPackageName());
                }

                // Set isPriceRange based on whether rangePricing is provided
                boolean hasRangePricing = !CollectionUtils.isEmpty(packageRequest.getRangePricing());
                boolean isPriceRange = packageRequest.getIsPriceRange() != null ? 
                    packageRequest.getIsPriceRange() : hasRangePricing;
                packageRequest.setIsPriceRange(isPriceRange);
            }

            // Create new packages with correct isPriceRange value
            productPackages = updateRequestDto.getPackages().stream()
                    .map(packageRequest -> productPackageMapper.toUpdateEntity(packageRequest, productId))
                    .toList();
            productPackageRepository.saveAll(productPackages);

            // Save range pricing for each package if provided
            for (int i = 0; i < productPackages.size(); i++) {
                ProductPackage pkg = productPackages.get(i);
                PackageRequest packageRequest = updateRequestDto.getPackages().get(i);

                // Save range pricing if provided
                if (!CollectionUtils.isEmpty(packageRequest.getRangePricing())) {
                    for (PackageRangePricingRequest rangePricingRequest : packageRequest.getRangePricing()) {
                        packageRangePricingService.createPackageRangePricing(rangePricingRequest, pkg.getId());
                    }
                }
            }
        }

        ProductResponse response = productMapper.toProductResponse(savedProduct);
        List<PackageRequest> packageResponses = productPackages.stream()
                .map(pkg -> {
                    PackageRequest pkgResponse = productPackageMapper.toResponse(pkg);
                    // Fetch and set range pricing
                    List<PackageRangePricingResponse> rangePricing = packageRangePricingService.getPricingByPackageId(pkg.getId());
                    pkgResponse.setRangePricingResponse(rangePricing);
                    return pkgResponse;
                })
                .toList();
        response.setPackages(packageResponses);
        return response;
    }

    private void checkUniqueProductName(String productName) {
        if (productRepository.existsByProductName(productName)) {
            throw new DuplicateNameException("Product name '" + productName + "' already exists.");
        }
    }

    @Override
    public List<ProductResponse> getAllProductsByStatus(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = Sort.Direction.fromString(order);
        Sort sort = Sort.by(
                Sort.Order.asc("displayOrder").nullsLast(),
                Sort.Order.by(sortBy).with(direction)
        );
        Pageable pageable = PageRequest.of(offset, pageSize, sort);
        Page<Product> products = productRepository.findByProductStatus(ProductStatus.ENABLED, pageable);

        List<ProductResponse> productResponses = productMapper.toProductResponseList(products.getContent());

        // Fetch and set packages for each product
        for (ProductResponse productResponse : productResponses) {
            List<ProductPackage> productPackages = productPackageRepository.findByProductId(productResponse.getProductId());
            List<PackageRequest> packages = enrichPackagesWithRangePricing(productPackages);
            productResponse.setPackages(packages);
            productResponse.setThumbnailUrl(fileService.getPath(productResponse.getThumbnailUrl()));
        }

        return productResponses;
    }


    @Override
    public String deleteProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND_WITH_ID + id));

        String productName = product.getProductName();
        productRepository.deleteById(id);

        return productName;
    }

    @Override
    public void exportProducts(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=products.csv");

        List<ProductResponse> products = getAllProducts(null, null, 0, 1000, "createdAt", "desc");

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {

            String[] header = {"ID", "Product Name", "Status", "Course IDs", "Created At", "Updated At"};
            writer.writeNext(header);

            for (ProductResponse product : products) {
                writer.writeNext(new String[]{
                        product.getProductId(),
                        product.getProductName(),
                        product.getProductStatus().toString(),
                        product.getCreatedAt().toString(),
                        product.getUpdatedAt().toString()
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export products to CSV", e);
        }
    }

    @Override
    public long getTotalProductCount() {
        return productRepository.count();
    }

    @Override
    public List<ProductResponse> getAllTrialProducts(String search, String status, Integer offset, Integer pageSize, String sortBy, String order) {
        // Get all distinct product IDs that have trial product packages with ENABLED status
        Criteria trialProductPackageCriteria = new Criteria();
        trialProductPackageCriteria.and("showInSite").is(true);
        trialProductPackageCriteria.and("packageStatus").is(PackageStatus.ENABLED);
        
        Query trialProductPackageQuery = new Query(trialProductPackageCriteria);
        trialProductPackageQuery.fields().include("productId");
        
        List<ProductPackage> trialProductPackages = mongoTemplate.find(trialProductPackageQuery, ProductPackage.class);
        List<String> trialProductIds = trialProductPackages.stream()
                .map(ProductPackage::getProductId)
                .distinct()
                .toList();
        
        if (trialProductIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Build criteria for filtering products - same structure as getAllProducts
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(
                Sort.Order.asc("displayOrder").nullsLast(),
                Sort.Order.by(sortBy).with(direction)
        );
        Pageable pageable = PageRequest.of(offset, pageSize, sort);

        Criteria criteria = new Criteria();
        
        // Filter products that have trial product packages
        criteria.and("id").in(trialProductIds);

        // Add search filter if provided
        if (!ObjectUtils.isEmpty(search)) {
            criteria.and("productName").regex(search, "i");
        }

        // Always filter by ENABLED status - only show enabled products
        criteria.and("productStatus").is(ProductStatus.ENABLED);

        // Create query with criteria and pagination - same as getAllProducts
        Query query = new Query(criteria).with(pageable);
        List<Product> products = mongoTemplate.find(query, Product.class);

        List<ProductResponse> productResponses = productMapper.toProductResponseList(products);

        // Fetch and set packages for each product - filter only ENABLED packages
        for (ProductResponse productResponse : productResponses) {
            List<ProductPackage> productPackages = productPackageRepository.findByProductId(productResponse.getProductId());
            // Filter only ENABLED packages
            List<ProductPackage> enabledPackages = productPackages.stream()
                    .filter(pkg -> PackageStatus.ENABLED.equals(pkg.getPackageStatus()))
                    .toList();
            List<PackageRequest> packages = enrichPackagesWithRangePricing(enabledPackages);
            productResponse.setPackages(packages);
        }

        return productResponses;
    }

    /**
     * Helper method to enrich packages with range pricing information
     */
    private List<PackageRequest> enrichPackagesWithRangePricing(List<ProductPackage> productPackages) {
        return productPackages.stream()
                .map(pkg -> {
                    PackageRequest pkgResponse = productPackageMapper.toResponse(pkg);
                    // Fetch and set range pricing
                    List<PackageRangePricingResponse> rangePricing = packageRangePricingService.getPricingByPackageId(pkg.getId());
                    pkgResponse.setRangePricingResponse(rangePricing);
                    return pkgResponse;
                })
                .toList();
    }

    @Override
    public long getTotalTrialProductCount(String search, String status) {
        // Get all distinct product IDs that have trial product packages with ENABLED status
        Criteria trialProductPackageCriteria = new Criteria();
        trialProductPackageCriteria.and("isTrial").is(true);
        trialProductPackageCriteria.and("packageStatus").is(PackageStatus.ENABLED);
        
        Query trialProductPackageQuery = new Query(trialProductPackageCriteria);
        trialProductPackageQuery.fields().include("productId");
        
        List<ProductPackage> trialProductPackages = mongoTemplate.find(trialProductPackageQuery, ProductPackage.class);
        List<String> trialProductIds = trialProductPackages.stream()
                .map(ProductPackage::getProductId)
                .distinct()
                .toList();
        
        if (trialProductIds.isEmpty()) {
            return 0;
        }

        // Build criteria for filtering products
        Criteria criteria = new Criteria();
        // Filter products that have trial product packages
        criteria.and("id").in(trialProductIds);

        // Add search filter if provided
        if (!ObjectUtils.isEmpty(search)) {
            criteria.and("productName").regex(search, "i");
        }

        // Always filter by ENABLED status - only show enabled products
        criteria.and("productStatus").is(ProductStatus.ENABLED);

        // Create query with criteria and count
        Query query = new Query(criteria);
        return mongoTemplate.count(query, Product.class);
    }

    @Override
    public List<String> deleteProductsByIds(List<String> ids) {
        List<String> deletedNames = new ArrayList<>();
        for (String id : ids) {
            deletedNames.add(deleteProductById(id));
        }
        return deletedNames;
    }

    @Override
    public List<String> updateProductsStatusByIds(List<String> ids, Status status) {
        List<String> updatedProductNames = new ArrayList<>();
        for (String id : ids) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND_WITH_ID + id));
            product.setProductStatus(ProductStatus.valueOf(status.name()));
            productRepository.save(product);
            updatedProductNames.add(product.getProductName());
        }
        return updatedProductNames;
    }

    @Override
    public void exportBulkProducts(List<String> ids, HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=bulk_products.csv");

        List<ProductResponse> products = ids.stream()
                .map(id -> {
                    Product product = productRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
                    return productMapper.toProductResponse(product);
                })
                .toList();

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            String[] header = {"ID", "Product Name", "Product Description", "Status", "Course IDs", "Created At", "Updated At"};
            writer.writeNext(header);

            for (ProductResponse product : products) {
                writer.writeNext(new String[]{
                        product.getProductId(),
                        product.getProductName(),
                        product.getProductDescription(),
                        product.getProductStatus().toString(),
                        product.getCreatedAt().toString(),
                        product.getUpdatedAt().toString()
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export bulk products to CSV", e);
        }
    }


    //assigned products to client admin

    @Override
    public List<ProductResponse> getAllAssignedProducts(String clientAdminId, String search, Integer offset, Integer pageSize, String sortBy, String order) {
        ClientProductIdListResponseDto clientProductData = getProductIdsByClient(clientAdminId);
        log.info("Client Product Data from registration service:{}", clientProductData);

        if (clientProductData.getClientProductDTOS() == null || clientProductData.getClientProductDTOS().isEmpty()) {
            return List.of();
        }

        //extract distinct product ids from clientProductData
        List<String> productIds = clientProductData.getClientProductDTOS().stream()
                .map(ClientProductDTO::getProductId)
                .distinct()
                .toList();

        List<String> packageIds = clientProductData.getClientProductDTOS().stream()
                .map(ClientProductDTO::getPackageId)
                .distinct()
                .toList();

        log.info("Product IDs assigned to client admin {}: {}", clientAdminId, productIds);
        log.info("Package IDs assigned to client admin {}: {}", clientAdminId, packageIds);

        //create Aggregate to fetch products by productIds with pagination and sorting

        List<AggregationOperation> operations = new ArrayList<>();

        // Match products by IDs
        operations.add(Aggregation.match(Criteria.where("_id").in(productIds)));

        // Apply search filter if provided
        if (search != null && !search.isEmpty()) {
            operations.add(Aggregation.match(
                    Criteria.where("productName").regex(search, "i")
            ));
        }

        // Lookup packages for each product
        operations.add(Aggregation.lookup("product_packages", "_id", "productId", "packages"));

        // Sort by displayOrder ascending (nulls last), then by sortBy/order
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort aggregationSort = Sort.by(
                Sort.Order.asc("displayOrder").nullsLast(),
                Sort.Order.by(sortBy).with(sortDirection)
        );
        operations.add(Aggregation.sort(aggregationSort));

        // Add pagination
        operations.add(Aggregation.skip((long) offset));
        operations.add(Aggregation.limit(pageSize));

        // Execute aggregation
        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<ProductWithPackages> results = mongoTemplate.aggregate(
                aggregation, "product", ProductWithPackages.class
        ).getMappedResults();

        // Step 4: Map to response DTOs
        return results.stream()
                .filter(product -> {
                    // Filter assigned packages based on packageIds
                    List<ProductPackage> filteredPackages = product.getPackages().stream()
                            .filter(pkg -> packageIds.contains(pkg.getId()))
                            .toList();
                    product.setPackages(filteredPackages);
                    return !filteredPackages.isEmpty();
                })
                .map(this::mapToProductResponse)
                .toList();

    }

    @Override
    public long getTotalAssignedProductCount(String clientAdminId, String search) {
        // Get assigned products from registration service
        ClientProductIdListResponseDto clientProductData = getProductIdsByClient(clientAdminId);

        if (clientProductData.getClientProductDTOS() == null || clientProductData.getClientProductDTOS().isEmpty()) {
            return 0;
        }

        // Extract unique product IDs
        List<String> productIds = clientProductData.getClientProductDTOS().stream()
                .map(ClientProductDTO::getProductId)
                .distinct()
                .toList();

        // Build query
        Query query = new Query(Criteria.where("_id").in(productIds));

        // Apply search filter if provided
        if (search != null && !search.isBlank()) {
            query.addCriteria(
                    new Criteria().orOperator(
                            Criteria.where("productName").regex(search, "i")
                    )
            );
        }

        return mongoTemplate.count(query, Product.class);
    }

    @Override
    public List<AssignedProductTagDto> getAssignedProductTags(String clientAdminId) {
        ClientProductIdListResponseDto clientProductData = getProductIdsByClient(clientAdminId);
        if (clientProductData.getClientProductDTOS() == null || clientProductData.getClientProductDTOS().isEmpty()) {
            return List.of();
        }

        List<String> productIds = clientProductData.getClientProductDTOS().stream()
                .map(ClientProductDTO::getProductId)
                .distinct()
                .toList();

        List<Product> products = productRepository.findAllById(productIds);
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .map(product -> AssignedProductTagDto.builder()
                        .productId(product.getId())
                        .productName(product.getProductName())
                        .tags(product.getTags())
                        .build())
                .toList();
    }

    private ClientProductIdListResponseDto getProductIdsByClient(String clientAdminId) {
        String url = registrationUrl + "/client/admin/products?clientAdminId=" + clientAdminId;
//        String url = "http://localhost:9090/registration/api/v1/client/admin/products?clientAdminId=" + clientAdminId;

        JsonNode response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null || response.get("data") == null) {
            throw new RuntimeException("No data field in registration response");
        }

        JsonNode dataNode = response.get("data");
        JsonNode clientIdsNode = dataNode.get("clientIds");
        JsonNode clientProductsNode = dataNode.get("clientProductDTOS");

        if (clientIdsNode == null || !clientIdsNode.isArray()) {
            throw new RuntimeException("Missing or invalid clientIds in registration response");
        }

        if (clientProductsNode == null || !clientProductsNode.isArray()) {
            throw new RuntimeException("Missing or invalid clientProductDTOS in registration response");
        }

        try {
            List<String> clientIds = objectMapper.treeToValue(clientIdsNode, new TypeReference<List<String>>() {
            });
            List<ClientProductDTO> clientProductDTOS = objectMapper
                    .readerFor(new TypeReference<List<ClientProductDTO>>() {
                    })
                    .readValue(clientProductsNode);

            return ClientProductIdListResponseDto.builder()
                    .clientIds(clientIds)
                    .clientProductDTOS(clientProductDTOS)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse clientIds or clientProductDTOS from registration response", e);
        }
    }

    private ProductResponse mapToProductResponse(ProductWithPackages product) {
        List<PackageRequest> packageRequests = product.getPackages().stream()
                .map(pkg -> {
                    // Convert feature IDs to FeatureDto objects with names
                    List<FeatureDto> features = List.of();
                    if (pkg.getFeatureId() != null && !pkg.getFeatureId().isEmpty()) {
                        List<Feature> featureEntities = featureRepository.findAllById(pkg.getFeatureId());
                        features = featureEntities.stream()
                                .map(feature -> FeatureDto.builder()
                                        .id(feature.getId())
                                        .name(feature.getFeatureName())
                                        .build())
                                .toList();
                    }

                    // Fetch range pricing for this package
                    List<PackageRangePricingResponse> rangePricing = packageRangePricingService.getPricingByPackageId(pkg.getId());

                    return PackageRequest.builder()
                            .id(pkg.getId())
                            .packageName(pkg.getName())
                            .productId(pkg.getProductId())
                            .features(features)
                            .price(pkg.getPrice())
                            .packageStatus(pkg.getPackageStatus())
                            .basePackageId(pkg.getBasePackageId())
                            .isPriceRange(pkg.getIsPriceRange())
                            .rangePricingResponse(rangePricing)
                            .build();
                })
                .toList();

        return ProductResponse.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productStatus(product.getProductStatus())
                .thumbnailUrl(product.getThumbnailUrl())
                .tags(product.getTags())
                .displayOrder(product.getDisplayOrder())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .lastModifiedBy(product.getLastModifiedBy())
                .packages(packageRequests)
                .build();
    }

    @Override
    public List<ProductPackageSimpleResponse> getPackagesByIds(Collection<String> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return List.of();
        }

        Set<String> uniquePackageIds = packageIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        if (uniquePackageIds.isEmpty()) {
            return List.of();
        }

        List<ProductPackage> packages = productPackageRepository.findAllById(uniquePackageIds);
        return packages.stream()
                .map(pkg -> ProductPackageSimpleResponse.builder()
                        .id(pkg.getId())
                        .name(pkg.getName())
                        .build())
                .toList();
    }
}
