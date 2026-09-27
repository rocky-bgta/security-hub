package com.aspire.asat.cms.dto.content;

import com.aspire.asat.cms.dto.content.formatting.ParagraphFormatting;
import com.aspire.asat.cms.dto.content.formatting.SubTitleFormatting;
import com.aspire.asat.cms.dto.content.formatting.TitleFormatting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfContentDto {

    @NotEmpty(message = "Title is required")
    private TitleFormatting titleFormatting;

    private SubTitleFormatting subTitleFormatting;

    private ParagraphFormatting paragraphFormatting;

    @NotEmpty(message = "PDF file name is required")
    private String pdfFileName;

    @NotEmpty(message = "PDF file URL is required")
    private String pdfFileURL;

    private Map<String, String> additionalProperties;
    private Map<String, Object> metadata;
}
