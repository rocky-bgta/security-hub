package com.aspire.asat.cms.dto.topic;

import com.aspire.asat.cms.dto.enums.PackageStatus;
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
public class PackageDetailsDto {
    private String id;
    private String name;
    private String productId;
    private List<String> featureId;
    private Double price;
    private Instant createdAt;
    private PackageStatus packageStatus;
    private String basePackageId;
}
