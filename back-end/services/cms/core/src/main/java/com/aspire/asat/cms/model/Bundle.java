package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.bundle.BundleDto;
import com.aspire.asat.cms.dto.bundle.BundleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "bundles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bundle {

    private String id; // MongoDB will automatically handle the ID
    private String bundleName;
    private String packageId;
    private List<String> featureIds;
    private double price;
    private Instant createdAt;
    private BundleStatus bundleStatus;

    // Convert BundleDto to Bundle entity (for saving in MongoDB)
    public static Bundle toEntity(String bundleId, BundleDto bundleDto) {
        return Bundle.builder()
                .id(bundleId) // This will be set manually
                .bundleName(bundleDto.getBundleName())
                .packageId(bundleDto.getPackageId())
                .featureIds(bundleDto.getFeatureIds())
                .price(bundleDto.getPrice())
                .createdAt(bundleDto.getCreatedAt())
                .bundleStatus(bundleDto.getBundleStatus())
                .build();
    }

    // Convert Bundle entity to BundleDto (for returning to the client)
    public static BundleDto toDto(Bundle bundle) {
        return BundleDto.builder()
                .id(bundle.getId())
                .bundleName(bundle.getBundleName())
                .packageId(bundle.getPackageId())
                .featureIds(bundle.getFeatureIds())
                .price(bundle.getPrice())
                .createdAt(bundle.getCreatedAt())
                .bundleStatus(bundle.getBundleStatus())
                .build();
    }

}
