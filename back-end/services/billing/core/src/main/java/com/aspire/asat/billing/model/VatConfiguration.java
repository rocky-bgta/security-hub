package com.aspire.asat.billing.model;

import com.aspire.asat.billing.RegionVatConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vat_configurations")
public class VatConfiguration {

    @Id
    private String id;  // countryId (UUID format)

    private String countryName;
    private double defaultVatRate;
    private boolean regionBased;
    private List<RegionVatConfig> regions;

    private Instant createdAt;
    private Instant updatedAt;
}
