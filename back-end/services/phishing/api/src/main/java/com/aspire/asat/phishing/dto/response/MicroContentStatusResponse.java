package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Status of an asynchronous micro content creation job, polled via the status endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicroContentStatusResponse {

    private String jobId;
    private DeepfakeJobStatus status;
    private String topicId;
    private boolean topicCreated;
    private int totalVideos;
    private int processedVideos;
    private List<MicroContentItemResult> items;
    private String failureReason;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MicroContentItemResult {
        private String chapterName;
        private String contentName;
        private String chapterId;
        private String contentId;
        private DeepfakeJobStatus status;
        private String error;
    }
}
