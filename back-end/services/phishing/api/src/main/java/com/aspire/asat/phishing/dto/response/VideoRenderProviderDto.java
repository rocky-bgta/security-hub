package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Implemented video render provider for the deepfake Step 6 dropdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRenderProviderDto {

    private VideoRenderProvider provider;
    private String displayName;
}
