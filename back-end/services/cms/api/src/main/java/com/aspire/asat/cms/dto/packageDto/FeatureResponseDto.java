package com.aspire.asat.cms.dto.packageDto;

import com.aspire.asat.cms.dto.enums.Availability;
import com.aspire.asat.cms.dto.enums.FeatureStatus;
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
public class FeatureResponseDto {
    private String id;
    private String featureName;
    private String featureDescription;
    private FeatureStatus featureStatus;
    private List<String> packageIds;
    private Availability availability;
    private String thumbnailUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private String lastModifiedBy;
}
