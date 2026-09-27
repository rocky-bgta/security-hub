package com.aspire.asat.registration.data.endUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Minimal AspireUser details for enrichment")
public class AspireUserBasicDto {

    @Schema(description = "User ID", example = "user-uuid-123")
    private String userId;

    @Schema(description = "User email", example = "john.doe@company.com")
    private String email;

    @Schema(description = "User full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User department", example = "IT")
    private String department;

    @Schema(description = "User risk group", example = "HIGH_RISK")
    private String riskGroup;
}
