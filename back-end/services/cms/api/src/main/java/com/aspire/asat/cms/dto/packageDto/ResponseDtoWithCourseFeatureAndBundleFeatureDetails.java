package com.aspire.asat.cms.dto.packageDto;

import com.aspire.asat.cms.dto.course.CourseResponseDto;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.packageDto.BundleWithFeatureDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDtoWithCourseFeatureAndBundleFeatureDetails {

    private String id;
    private String packageName;
    private String packageDescription;
    private double price;
    private PackageStatus packageStatus;
    private List<CourseResponseDto> courseIds;
    private String thumbnailUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private String lastModifiedBy;
    private List<BundleWithFeatureDto> bundlesIds; // now includes enriched features
}
