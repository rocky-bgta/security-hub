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
public class TagReqDto {

    @NotBlank(message = "Tag name must not be empty")
    private String name;

    private String description;
}
