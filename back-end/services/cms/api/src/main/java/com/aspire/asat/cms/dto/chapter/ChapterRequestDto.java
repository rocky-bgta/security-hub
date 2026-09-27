package com.aspire.asat.cms.dto.chapter;

import com.aspire.asat.cms.dto.enums.ChapterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterRequestDto {

    @NotNull(message = "Topic id is required")
    private String topicId;

    @NotBlank(message = "chapter name is required")
    private String chapterName;

    private String chapterDescription;

    @NotNull(message = "Position in course is required")
    private Integer position;

    @NotNull(message = "Chapter status is required")
    private ChapterStatus chapterStatus;


    private List<String> contentIds;
}
