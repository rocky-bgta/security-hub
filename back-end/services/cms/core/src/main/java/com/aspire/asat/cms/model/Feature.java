package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.enums.Availability;
import com.aspire.asat.cms.dto.enums.FeatureStatus;
import com.aspire.asat.cms.dto.featureDto.RequestDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDto;

import com.aspire.asat.cms.dto.featureDto.ResponseDtoWithPackageDetails;
import com.aspire.asat.cms.dto.featureDto.UpdateRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Feature {

    private String id;
    private String featureName;
    private String featureDescription;
    private FeatureStatus featureStatus;
    private List<String> packageIds;
    private Availability availability;
    private String thumbnailUrl;
    private Instant createdAt;
    private Instant updatedAt;

    @LastModifiedBy
    private String lastModifiedBy;

    public static ResponseDto toFeatureDto(Feature feature) {
        return ResponseDto.builder().
                id(feature.getId()).
                featureName(feature.getFeatureName()).
                featureDescription(feature.getFeatureDescription()).
                featureStatus(feature.getFeatureStatus()).
                packageIds(feature.getPackageIds()).
                availability(feature.getAvailability()).
                thumbnailUrl(feature.getThumbnailUrl()).
                createdAt(feature.getCreatedAt()).
                updatedAt(feature.getUpdatedAt()).
                lastModifiedBy(feature.getLastModifiedBy()).
                build();
    }

    public static Feature toFeature(String featureId, RequestDto requestDto, Instant createdAt, Instant updatedAt) {
        return Feature.builder().
                id(featureId).
                featureName(requestDto.getFeatureName()).
                featureDescription(requestDto.getFeatureDescription()).
                featureStatus(requestDto.getFeatureStatus()).
                packageIds(requestDto.getPackageIds()).
                availability(requestDto.getAvailability()).
                thumbnailUrl(requestDto.getThumbnailUrl()).
                createdAt(createdAt).
                updatedAt(updatedAt).
                build();
    }

    public static Feature toUpdateFeature(String featureId, UpdateRequestDto updateRequestDto) {
        return Feature.builder().
                id(featureId).
                featureName(updateRequestDto.getFeatureName()).
                featureDescription(updateRequestDto.getFeatureDescription()).
                featureStatus(updateRequestDto.getFeatureStatus()).
                packageIds(updateRequestDto.getPackageIds()).
                availability(updateRequestDto.getAvailability()).
                thumbnailUrl(updateRequestDto.getThumbnailUrl()).
                createdAt(updateRequestDto.getCreatedAt()).
                updatedAt(Instant.now()).
                build();
    }

    public static ResponseDtoWithPackageDetails toFeatureDtoWithPackageDetails(Feature feature, List<com.aspire.asat.cms.dto.packageDto.ResponseDto> packageDetailDto) {
        return ResponseDtoWithPackageDetails.builder().
                id(feature.getId()).
                featureName(feature.getFeatureName()).
                featureDescription(feature.getFeatureDescription()).
                featureStatus(feature.getFeatureStatus()).
                packageIds(packageDetailDto).
                availability(feature.getAvailability()).
                thumbnailUrl(feature.getThumbnailUrl()).
                createdAt(feature.getCreatedAt()).
                updatedAt(feature.getUpdatedAt()).
                lastModifiedBy(feature.getLastModifiedBy()).
                build();
    }
}
