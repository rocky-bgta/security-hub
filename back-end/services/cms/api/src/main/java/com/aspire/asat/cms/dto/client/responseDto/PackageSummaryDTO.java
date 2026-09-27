package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageSummaryDTO {
    private String id;
    private String packageName;
    private double price;
    private String thumbnailUrl;
    private String status; // ✅ Add this
}
