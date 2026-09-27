package com.aspire.asat.registration.data.mspUser.request;

import com.aspire.asat.registration.data.enums.MspStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating MSP user status")
public class MspStatusUpdateDto {

    @NotNull(message = "Status cannot be null")
    @Schema(description = "New status for the MSP user", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private MspStatus status;
}

