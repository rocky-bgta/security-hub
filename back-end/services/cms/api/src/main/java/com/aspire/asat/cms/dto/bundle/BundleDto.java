package com.aspire.asat.cms.dto.bundle;

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
public class BundleDto {

    private String id;
    private String bundleName;
    private String packageId;
    private List<String> featureIds;
    private double price;
    private Instant createdAt;
    private BundleStatus bundleStatus;

}
