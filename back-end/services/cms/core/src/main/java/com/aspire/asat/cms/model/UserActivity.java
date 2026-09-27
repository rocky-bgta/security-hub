package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {

    private String userId;
    private String sessionId;
    @Indexed
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration;  // Duration in seconds
    @Indexed
    private LocalDateTime lastAvailableTime;

}
