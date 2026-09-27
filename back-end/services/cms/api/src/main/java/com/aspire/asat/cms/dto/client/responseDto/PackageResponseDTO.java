package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponseDTO {

    @NotBlank
    private String packageId;

    @NotBlank
    private String packageName;

    @NotNull
    @Min(1)
    private Integer totalCourses;

    @NotNull
    @Min(0)
    private Integer completedCourses;

    @NotNull
    private Double progress;

    @NotBlank
    private String validity;

    private String assignedDate;

    private String expireDate;

    private boolean expired;
}
