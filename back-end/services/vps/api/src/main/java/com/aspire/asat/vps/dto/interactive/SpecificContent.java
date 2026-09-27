package com.aspire.asat.vps.dto.interactive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificContent {
    private BackgroundFormatting backgroundFormatting;
    private InteractiveVideo interactiveVideo;
    /** Multi-language videos. Request: array [{ "id", "language", "videoUrl", ... }]. Storage in CMS: Map<language, InteractiveVideo>. */
    private Object interactiveVideoByLanguage;
    private String language;
    private List<ContentListItem> contentList;
    private boolean updateContent;
    private String captionUrl;
    private String videoTitle;
    private Metadata metadata;
}
