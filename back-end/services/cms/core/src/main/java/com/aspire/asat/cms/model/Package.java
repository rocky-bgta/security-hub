package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.bundle.BundleDto;
import com.aspire.asat.cms.dto.course.CourseResponseDto;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.packageDto.RequestDto;
import com.aspire.asat.cms.dto.packageDto.ResponseDto;
import com.aspire.asat.cms.dto.packageDto.ResponseDtoWithCourseFeatureDetails;
import com.aspire.asat.cms.dto.packageDto.UpdateRequestDto;
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

public class Package {

    private String id;
    private String packageName;
    private String packageDescription;
    private double price;
    private PackageStatus packageStatus;
    private List<String> courseIds;
    private List<String> featureIds;
    private String thumbnailUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> bundlesIds;

    @LastModifiedBy
    private String lastModifiedBy;



    public static ResponseDto toPackageDto(Package packages) {
        return ResponseDto.builder().
                id(packages.getId()).
                packageName(packages.getPackageName()).
                packageDescription(packages.getPackageDescription()).
                price(packages.getPrice()).
                packageStatus(packages.getPackageStatus()).
                courseIds(packages.getCourseIds()).
                featureIds(packages.getFeatureIds()).
                thumbnailUrl(packages.getThumbnailUrl()).
                createdAt(packages.getCreatedAt()).
                updatedAt(packages.getUpdatedAt()).
                lastModifiedBy(packages.getLastModifiedBy()).
                bundlesIds(packages.getBundlesIds()).
                build();
    }

    public static Package toPackage(String packageId, RequestDto requestDto, Instant createdAt, Instant updatedAt) {
        return Package.builder().
                id(packageId).
                packageName(requestDto.getPackageName()).
                packageDescription(requestDto.getPackageDescription()).
                price(requestDto.getPrice()).
                packageStatus(requestDto.getPackageStatus()).
                courseIds(requestDto.getCourseIds()).
                featureIds(requestDto.getFeatureIds()).
                thumbnailUrl(requestDto.getThumbnailUrl()).
                createdAt(createdAt).
                updatedAt(updatedAt).
                build();
    }

    public static Package toUpdatePackage(String packageId, UpdateRequestDto updateRequestDto) {
        return Package.builder().
                id(packageId).
                packageName(updateRequestDto.getPackageName()).
                packageDescription(updateRequestDto.getPackageDescription()).
                price(updateRequestDto.getPrice()).
                packageStatus(updateRequestDto.getPackageStatus()).
                courseIds(updateRequestDto.getCourseIds()).
                featureIds(updateRequestDto.getFeatureIds()).
                thumbnailUrl(updateRequestDto.getThumbnailUrl()).
                createdAt(updateRequestDto.getCreatedAt()).
                updatedAt(Instant.now()).
                build();
    }

    public static ResponseDtoWithCourseFeatureDetails toPackageDtoWithCourseFeatureDetails(Package packages,List<CourseResponseDto> courseDetails, List<BundleDto> bundlesIds) {
        return ResponseDtoWithCourseFeatureDetails.builder()
                .id(packages.getId())
                .packageName(packages.getPackageName())
                .packageDescription(packages.getPackageDescription())
                .price(packages.getPrice())
                .packageStatus(packages.getPackageStatus())
                .courseIds(courseDetails)
                .thumbnailUrl(packages.getThumbnailUrl())
                .createdAt(packages.getCreatedAt())
                .updatedAt(packages.getUpdatedAt())
                .lastModifiedBy(packages.getLastModifiedBy())
                .bundlesIds(bundlesIds)
                .build();
    }

}
