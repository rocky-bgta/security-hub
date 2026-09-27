package com.aspire.asat.cms.dto.content.question;

import com.aspire.asat.cms.dto.content.formatting.BackgroundFormatting;
import com.aspire.asat.cms.dto.enums.Score;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class QuestionRequestDto {

    private String question;
    private List<Option> options;
    private BackgroundFormatting backgroundFormatting;
    private Boolean scorable;
    private Score score;

}
