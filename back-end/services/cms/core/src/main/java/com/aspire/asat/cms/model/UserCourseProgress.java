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
@Document(collection = "user_course_progress")
@CompoundIndex(name = "user_course_progress_idx", def = "{'userId': 1, 'courseId': 1}", unique = true)
public class UserCourseProgress {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    @Indexed
    private String packageId;

    private double progress;

    private List<String> completedContentIds;

    private Instant lastSynced;

    public UserCourseProgress(String userId, String courseId, double progress, List<String> completedContentIds, Instant lastSynced) {
        this.userId = userId;
        this.courseId = courseId;
        this.progress = progress;
        this.completedContentIds = completedContentIds;
        this.lastSynced = lastSynced;
    }

}
