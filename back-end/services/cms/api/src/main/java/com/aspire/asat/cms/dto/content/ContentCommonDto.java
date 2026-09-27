package com.aspire.asat.cms.dto.content;

import com.aspire.asat.cms.dto.enums.CommonStatus;
import com.aspire.asat.cms.dto.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ContentCommonDto {

    @NotBlank(message = "Content name is required")
    private String contentName;

    private String description;

    @NotEmpty(message = "At least one chapter must be associated with the content")
    private ContentType contentType;

    @NotEmpty(message = "status is required")
    private CommonStatus status;

    @NotEmpty(message = "Author is required")
    private String author;

    @NotEmpty(message = "At least one chapter must be associated with the content")
    private List<String> chapterIds;

    @NotEmpty(message = "At least one tag must be associated with the content")
    private List<String>tags;

}
