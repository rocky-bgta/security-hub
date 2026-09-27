package com.aspire.asat.cms.dto.interactive;

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
    /** Dynamic card title for this specific video (e.g.,English, Bangla). */
    private String cardTitle;
    /** Language code (e.g., en, hi, bn). Set when building response. */
    private String language;
    /** True if this is the default video for the requester's locale. Set when building response. */
    @JsonProperty("default")
    private Boolean defaultVideo;
    private List<ContentListItem> contentList;
}
