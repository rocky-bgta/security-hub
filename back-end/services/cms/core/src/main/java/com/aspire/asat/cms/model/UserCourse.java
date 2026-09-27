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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_courses")
@CompoundIndex(name = "user_course_idx", def = "{'userId': 1, 'courseId': 1}", unique = true)
public class UserCourse {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String courseId;

    @Indexed
    private String packageId;

    @Indexed
    private String status;

    private boolean isSaved;

    private String certificateLink;

    private String ImageCertificateLink;

    // Manually add setter if Lombok isn't working for some reason
    public void setIsSaved(boolean isSaved) {
        this.isSaved = isSaved;
    }
}
