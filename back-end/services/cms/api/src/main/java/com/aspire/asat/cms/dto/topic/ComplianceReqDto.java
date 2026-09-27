package com.aspire.asat.cms.dto.topic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceReqDto {
    @NotEmpty(message = "Compliance name must not be empty")
    private String complianceName;
    @NotBlank(message = "Acronym must not be blank")
    private String acronym; // PCI, HIPAA, GDPR, FedRAMP
    private String description;
    @NotNull(message = "Sort order must not be null")
    private Integer sortOrder;
}
