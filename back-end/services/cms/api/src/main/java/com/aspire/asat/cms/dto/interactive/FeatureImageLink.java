package com.aspire.asat.cms.dto.interactive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureImageLink {
    private String featureImageLink;
    private boolean updateContent;
}
