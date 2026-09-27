package com.aspire.asat.cms.dto.interactive;

import com.aspire.asat.cms.dto.content.ContentCommonDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentUpdateRequest {
    private ContentCommonDto common;
    private SpecificContent specific;
}
