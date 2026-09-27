package com.aspire.asat.cms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardTopicProgressItemDto {

    private String topicId;

    private String name;

    private String start_date;

    private String expire_date;

    private String progress; // e.g., "80%"
}


