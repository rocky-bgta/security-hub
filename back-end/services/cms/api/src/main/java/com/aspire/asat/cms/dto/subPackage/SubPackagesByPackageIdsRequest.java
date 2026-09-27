package com.aspire.asat.cms.dto.subPackage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to fetch sub-packages by package IDs")
public class SubPackagesByPackageIdsRequest {

    @NotEmpty
    @Schema(description = "List of package IDs to look up", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> packageIds;

    @Schema(description = "Optional client admin ID to prefer client-specific sub-packages")
    private String clientAdminId;
}
