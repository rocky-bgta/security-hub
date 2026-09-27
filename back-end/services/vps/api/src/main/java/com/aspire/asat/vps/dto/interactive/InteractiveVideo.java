package com.aspire.asat.vps.dto.interactive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InteractiveVideo {

    private String isProcessing;
    private String processingStatus;
    private String videoUrl;
    private String processVideoUrl;
    private String videoLength;
    private String id;
    /** Dynamic card title for this specific video (e.g., Video Card_English). */
    private String cardTitle;
    /** Language code (e.g., en, hi, bn). Same as CMS. */
    private String language;
    /** True if default video for locale. Same as CMS. */
    @JsonProperty("default")
    private Boolean defaultVideo;
    private List<ContentListItem> contentList;
}
