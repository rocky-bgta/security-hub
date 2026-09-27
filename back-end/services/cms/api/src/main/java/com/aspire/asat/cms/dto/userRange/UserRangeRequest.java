package com.aspire.asat.cms.dto.userRange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRangeRequest {

    @NotBlank(message = "Range name cannot be blank")
    private String rangeName;

    @NotNull(message = "Minimum users cannot be null")
    @Min(value = 1, message = "Minimum users must be at least 1")
    private Integer minUsers;

    @Min(value = 1, message = "Maximum users must be at least 1")
    private Integer maxUsers;  // Can be null for unlimited

    private String description;

    @Builder.Default
    private Boolean isDefault = false;
}

