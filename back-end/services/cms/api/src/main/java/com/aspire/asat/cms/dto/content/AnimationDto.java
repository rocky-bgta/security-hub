package com.aspire.asat.cms.dto.content;

import com.aspire.asat.cms.dto.content.formatting.BackgroundFormatting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AnimationDto {

    @NotEmpty(message = "Title is required")
    private String title;
    private String animationLink;
    private String animationCaption;
    private BackgroundFormatting backgroundFormatting;

}
