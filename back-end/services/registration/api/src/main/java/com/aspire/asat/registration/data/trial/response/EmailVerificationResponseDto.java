package com.aspire.asat.registration.data.trial.response;

import com.aspire.asat.registration.data.enums.NextDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after email verification")
public class EmailVerificationResponseDto {

    @Schema(description = "Whether email was verified successfully", example = "true")
    private Boolean verified;

    @Schema(description = "Message about verification status", example = "Email verified successfully!")
    private String message;

    @Schema(description = "Email address that was verified", example = "mdtest001@yopmail.com")
    private String email;

    @Schema(description = "Selected products with existing-trial flags")
    private List<ProductsDataResponse> productsData;

    @Schema(description = "Frontend next step: verify if the email is new, onboard if a user already exists", example = "verify")
    private NextDirection nextDirection;
}

