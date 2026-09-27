package com.aspire.asat.registration.data.clientAdmin.response;


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
