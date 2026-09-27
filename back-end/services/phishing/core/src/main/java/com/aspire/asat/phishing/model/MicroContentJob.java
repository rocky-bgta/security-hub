package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Tracks an asynchronous micro content creation job. Each deepfake video becomes its own CMS
 * topic (topic + chapter + VIDEO content). Persisted so the SQS worker (which has no request
 * context) can process it and so clients can poll status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "micro_content_jobs")
public class MicroContentJob {

    @Id
    private String id;

    @Indexed(unique = true)
    private UUID jobId;

    @Indexed
    private String clientId;

    /** Display name resolved from CurrentUserContext at enqueue (for CMS topic naming). */
    private String clientName;

    /** CMS topic name for this video's dedicated topic. */
    private String topicName;

    private List<VideoItem> videos;

    @Indexed
    @Builder.Default
    private DeepfakeJobStatus status = DeepfakeJobStatus.PENDING;

    private String topicId;

    private boolean topicCreated;

    private List<ItemResult> items;

    private int totalVideos;

    private int processedVideos;

    private String failureReason;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoItem {
        /** Deepfake renderId that sourced this snapshot. */
        @Indexed
        private String deepfakeVideoId;
        private String chapterName;
        private String chapterDescription;
        private String contentName;
        /** Durable S3 key (or URL fallback) for the deepfake video artifact. */
        private String videoUrl;
        /**
         * Durable S3 key for the deepfake thumbnail, used as the CMS topic thumbnailUrl.
         * Null when the render completed without a thumbnail.
         */
        private String thumbnailUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResult {
        private String chapterName;
        private String contentName;
        private String chapterId;
        private String contentId;
        @Builder.Default
        private DeepfakeJobStatus status = DeepfakeJobStatus.PENDING;
        private String error;
    }
}
