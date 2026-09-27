package com.aspire.asat.cms.dto.client.responseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing subpackage ID and name for a user")
public class UserSubPackageListResponseDTO {

    @Schema(description = "SubPackage ID", example = "sub-package-uuid-123")
    private String subPackageId;

    @Schema(description = "SubPackage name", example = "Cybersecurity Fundamentals")
    private String subPackageName;
}

