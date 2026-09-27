package com.aspire.asat.cms.mapper;

import com.aspire.asat.cms.dto.product.FeatureDto;
import com.aspire.asat.cms.dto.product.PackageRequest;
import com.aspire.asat.cms.model.Feature;
import com.aspire.asat.cms.model.ProductPackage;
import com.aspire.asat.cms.repository.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ProductPackageMapper {
    
    @Autowired
    private FeatureRepository featureRepository;
    
    public ProductPackage toEntity(PackageRequest request, String productId) {
        // Extract feature IDs from the features list
        List<String> featureIds = request.getFeatures() != null ? 
            request.getFeatures().stream()
                .map(FeatureDto::getId)
                .toList() : List.of();
                
        return ProductPackage.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getPackageName())
                .productId(productId)
                .featureId(featureIds)
                .price(request.getPrice())
                .yearlyPrice(request.getYearlyPrice())
                .createdAt(Instant.now())
                .packageStatus(request.getPackageStatus())
                .basePackageId(request.getBasePackageId())
                .isTrial(request.getIsTrial() != null ? request.getIsTrial() : false)
                .showInSite(request.getShowInSite() != null ? request.getShowInSite() : false)
                .isPriceRange(request.getIsPriceRange() != null ? request.getIsPriceRange() : false)
                .build();
    }

    //for update
    public ProductPackage toUpdateEntity(PackageRequest request, String productId) {
        // Extract feature IDs from the features list
        List<String> featureIds = request.getFeatures() != null ?
                request.getFeatures().stream()
                        .map(FeatureDto::getId)
                        .toList() : List.of();

        return ProductPackage.builder()
                .id(request.getId())
                .name(request.getPackageName())
                .productId(productId)
                .featureId(featureIds)
                .price(request.getPrice())
                .yearlyPrice(request.getYearlyPrice())
                .createdAt(Instant.now())
                .packageStatus(request.getPackageStatus())
                .basePackageId(request.getBasePackageId())
                .isTrial(request.getIsTrial() != null ? request.getIsTrial() : false)
                .showInSite(request.getShowInSite() != null ? request.getShowInSite() : false)
                .isPriceRange(request.getIsPriceRange() != null ? request.getIsPriceRange() : false)
                .build();
    }



    public PackageRequest toResponse(ProductPackage productPackage) {
        // Convert feature IDs to FeatureDto objects with names
        List<FeatureDto> features = List.of();
        if (productPackage.getFeatureId() != null && !productPackage.getFeatureId().isEmpty()) {
            List<Feature> featureEntities = featureRepository.findAllById(productPackage.getFeatureId());
            features = featureEntities.stream()
                    .map(feature -> FeatureDto.builder()
                            .id(feature.getId())
                            .name(feature.getFeatureName())
                            .build())
                    .toList();
        }
        
        return PackageRequest.builder()
                .id(productPackage.getId())
                .packageName(productPackage.getName())
                .productId(productPackage.getProductId())
                .features(features)
                .price(productPackage.getPrice())
                .yearlyPrice(productPackage.getYearlyPrice())
                .packageStatus(productPackage.getPackageStatus())
                .basePackageId(productPackage.getBasePackageId())
                .isTrial(productPackage.getIsTrial())
                .showInSite(productPackage.getShowInSite())
                .isPriceRange(productPackage.getIsPriceRange())
                .build();
    }
}

