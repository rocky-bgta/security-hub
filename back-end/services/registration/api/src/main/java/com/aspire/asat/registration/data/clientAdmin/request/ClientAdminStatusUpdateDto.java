package com.aspire.asat.registration.data.clientAdmin.request;

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
@Schema(description = "Request DTO for updating client admin status")
public class ClientAdminStatusUpdateDto {

    @NotNull(message = "Status is required")
    @Schema(description = "New status for the client admin", example = "ACTIVE", required = true)
    private AdminStatus status;
}

