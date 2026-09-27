package com.aspire.asat.cms.dto.content.question;

import com.aspire.asat.cms.dto.content.formatting.BackgroundFormatting;
import com.aspire.asat.cms.dto.enums.Score;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class QuestionResponseDto {

    private UUID id;
    private String question;
    private List<Option> options;
    private BackgroundFormatting backgroundFormatting;
    private Boolean scorable;
    private Score score;
    private Instant createdAt;
    private Instant updatedAt;

}
