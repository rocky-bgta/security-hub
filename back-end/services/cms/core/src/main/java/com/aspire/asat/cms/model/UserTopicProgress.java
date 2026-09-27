package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "user_topics")
@CompoundIndex(name = "user_topics_idx", def = "{'userId': 1, 'topicId': 1}", unique = true)
public class UserTopicProgress {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String topicId;

    @Indexed
    private String subPackageId;

    private double progress;

    private List<String> completedContentIds;

    private Instant lastSynced;

    private Boolean isBookmarked = false;

    @Indexed
    private String status;

    private boolean isSaved;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    // Manual setter for isBookmarked to ensure proper method name
    public void setIsBookmarked(boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }

    // Manual setter for isSaved to ensure proper method name
    public void setIsSaved(boolean isSaved) {
        this.isSaved = isSaved;
    }
}
