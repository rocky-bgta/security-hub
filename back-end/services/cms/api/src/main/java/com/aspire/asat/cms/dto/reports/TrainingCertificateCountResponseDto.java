package com.aspire.asat.cms.dto.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Training completed and certificate earned counts for a client admin")
public class TrainingCertificateCountResponseDto {

    @Schema(description = "Count of user_subpackages with status COMPLETED for the client admin", example = "0")
    private long trainingCompletedCount;

    @Schema(description = "Count of user_certificates for the client admin", example = "0")
    private long certificateEarnedCount;
}
