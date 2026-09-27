package com.aspire.asat.cms.dto.packageDto;

import com.aspire.asat.cms.dto.bundle.BundleStatus;
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
public class BundleWithFeatureDto {

    private String id;
    private String bundleName;
    private String packageId;
    private double price;
    private Instant createdAt;
    private BundleStatus bundleStatus;
    private List<FeatureResponseDto> features; // enriched features inside each bundle
}
