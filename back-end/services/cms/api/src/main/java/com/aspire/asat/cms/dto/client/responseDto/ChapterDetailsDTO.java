package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDetailsDTO {

    @NotBlank
    private String chapterId;

    @NotBlank
    private String chapterName;

    private String chapterDescription;

    @NotNull
    private List<ContentStatusDTO> contents;
}
