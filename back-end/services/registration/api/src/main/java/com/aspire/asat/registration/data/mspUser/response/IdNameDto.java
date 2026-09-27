package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic ID and Name pair")
public class IdNameDto {

    @Schema(description = "ID", example = "tier-id-123")
    private String id;

    @Schema(description = "Name", example = "Gold Tier")
    private String name;
}

