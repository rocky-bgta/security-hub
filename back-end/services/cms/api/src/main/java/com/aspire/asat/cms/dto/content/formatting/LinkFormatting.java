package com.aspire.asat.cms.dto.content.formatting;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LinkFormatting {

    private String linkText;
    private String url;
    private String linkColor;

}
