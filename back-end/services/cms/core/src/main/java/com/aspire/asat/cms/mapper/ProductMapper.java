package com.aspire.asat.cms.mapper;

import com.aspire.asat.cms.dto.course.CourseResponseDto;
import com.aspire.asat.cms.dto.product.ProductCreationRequest;
import com.aspire.asat.cms.dto.product.ProductResponse;
import com.aspire.asat.cms.dto.product.ResponseDto;
import com.aspire.asat.cms.dto.product.ResponseDtoWithCourseDetails;
import com.aspire.asat.cms.dto.product.UpdateRequestDto;
import com.aspire.asat.cms.model.Product;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class ProductMapper {

    /**
     * Converts ProductCreationRequest to Product entity
     */
    public Product toEntity(ProductCreationRequest request) {
        return Product.builder()
                .id(UUID.randomUUID().toString())
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .productStatus(request.getProductStatus())
                .thumbnailUrl(request.getThumbnailUrl())
                .tags(normalizeTags(request.getTags()))
                .isTrial(request.getIsTrial() != null ? request.getIsTrial() : false)
                .showInSite(request.getShowInSite() != null ? request.getShowInSite() : false)
                .displayOrder(request.getDisplayOrder())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Converts Product entity to ProductResponse
     */
    public ProductResponse toCreateResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productStatus(product.getProductStatus())
                .thumbnailUrl(product.getThumbnailUrl())
                .tags(product.getTags())
                .isTrial(product.getIsTrial())
                .showInSite(product.getShowInSite())
                .displayOrder(product.getDisplayOrder())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .lastModifiedBy(product.getLastModifiedBy())
                .build();
    }

    /**
     * Converts Product entity to ProductResponse
     */
    public ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productStatus(product.getProductStatus())
                .thumbnailUrl(product.getThumbnailUrl())
                .tags(product.getTags())
                .isTrial(product.getIsTrial())
                .showInSite(product.getShowInSite())
                .displayOrder(product.getDisplayOrder())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .lastModifiedBy(product.getLastModifiedBy())
                .build();
    }

    /**
     * Converts Product entity to ResponseDto
     */
    public ResponseDto toResponseDto(Product product) {
        return ResponseDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productStatus(product.getProductStatus())
                .thumbnailUrl(product.getThumbnailUrl())
                .tags(product.getTags())
                .isTrial(product.getIsTrial())
                .showInSite(product.getShowInSite())
                .displayOrder(product.getDisplayOrder())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .lastModifiedBy(product.getLastModifiedBy())
                .build();
    }

    /**
     * Converts Product entity to ResponseDtoWithCourseDetails
     */
    public ResponseDtoWithCourseDetails toResponseDtoWithCourseDetails(Product product, List<CourseResponseDto> courseDetails) {
        return ResponseDtoWithCourseDetails.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productStatus(product.getProductStatus())
                .courseIds(courseDetails)
                .thumbnailUrl(product.getThumbnailUrl())
                .tags(product.getTags())
                .isTrial(product.getIsTrial())
                .showInSite(product.getShowInSite())
                .displayOrder(product.getDisplayOrder())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .lastModifiedBy(product.getLastModifiedBy())
                .build();
    }

    /**
     * Converts UpdateRequestDto to Product entity for updates
     */
    public Product toUpdateEntity(String id, UpdateRequestDto request) {
        return Product.builder()
                .id(id)
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .productStatus(request.getProductStatus())
                .thumbnailUrl(request.getThumbnailUrl())
                .displayOrder(request.getDisplayOrder())
                .createdAt(request.getCreatedAt())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Converts list of Products to list of ProductResponses
     */
    public List<ProductResponse> toProductResponseList(List<Product> products) {
        return products.stream()
                .map(this::toProductResponse)
                .toList();
    }

    /**
     * Converts list of Products to list of ResponseDtos
     */
    public List<ResponseDto> toResponseDtoList(List<Product> products) {
        return products.stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Converts ProductCreationRequest to ProductCreateResponse (for validation purposes)
     */
    public ProductResponse toCreateResponseFromRequest(ProductCreationRequest request) {
        return ProductResponse.builder()
                .productName(request.getProductName())
                .productStatus(request.getProductStatus())
                .thumbnailUrl(request.getThumbnailUrl())
                .tags(normalizeTags(request.getTags()))
                .packages(request.getPackages())
                .build();
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return Collections.emptyList();
        }

        return tags.stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
