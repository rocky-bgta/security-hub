package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.enums.PackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "product_packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackage {
    @Id
    private String id;

    private String name;

    private String productId;
    private List<String> featureId;

    private Double price;      // Monthly price
    private Double yearlyPrice; // Yearly price
    private Instant createdAt;
    private PackageStatus packageStatus;
    private String basePackageId;
    private Boolean isTrial = false;
    private Boolean showInSite = false;
    private Boolean isPriceRange = false;  // Indicates whether package uses range-based pricing
}