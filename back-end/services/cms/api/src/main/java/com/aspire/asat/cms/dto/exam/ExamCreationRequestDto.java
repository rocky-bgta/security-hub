package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreationRequestDto {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Sub-package ID is required")
    private String subPackageId;

    private String examTitle;

    private String examDescription;
}
