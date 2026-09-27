package com.aspire.asat.registration.data.clientAdmin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing MSP ID for a client admin")
public class MspIdResponseDto {

    @Schema(description = "Client admin ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String clientAdminId;

    @Schema(description = "MSP ID associated with the client admin", example = "msp-uuid-123")
    private String mspId;
}

