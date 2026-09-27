package com.aspire.asat.cms.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "user_content_status")
@CompoundIndex(name = "user_content_idx", def = "{'userId': 1, 'contentId': 1}", unique = true)
public class UserContentStatus {

    @Id
    private String id; // UUID as string

    @Indexed
    private String userId;

    @Indexed
    private String topicId;

    @Indexed
    private String subPackageId;

    private String contentId;

    private boolean isDone;

    // Legacy fields for backward compatibility
    @Indexed
    private String courseId;

    @Indexed
    private String packageId;

    public void setIsDone(boolean done) {
        isDone = done;
    }
}
