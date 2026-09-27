package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Phishing-local mirror of CMS {@code ContentReqDto} (the {@code common} + {@code specific} wrapper)
 * for creating a VIDEO content. Sent to {@code POST {service.cms.url}/contents}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsContentCreateRequest {

    private Common common;
    private Specific specific;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Common {
        private String contentName;
        private String contentType;
        private String status;
        private List<String> chapterIds;
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Specific {
        private BackgroundFormatting backgroundFormatting;
        private String captionUrl;
        private InteractiveVideo interactiveVideo;
        private Metadata metadata;
        private boolean updateContent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BackgroundFormatting {
        private String textColor;
        private String backgroundColor;
        private String backgroundImage;
        private String backgroundOpacity;
        private String tone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractiveVideo {
        private String id;
        private String videoUrl;
        @JsonProperty("isProcessing")
        private boolean isProcessing;
        private String processingStatus;
        private String processVideoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Metadata {
        private AdditionalProp additionalProp1;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdditionalProp {
        private String videoText;
    }
}
