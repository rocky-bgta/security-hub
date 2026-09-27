package com.aspire.asat.cms.dto.tag;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagStatusUpdateDto {

    @NotBlank(message = "Status is required")
    private String status;
}
