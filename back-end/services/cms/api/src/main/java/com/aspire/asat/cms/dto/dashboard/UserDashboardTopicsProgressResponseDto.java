package com.aspire.asat.cms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardTopicsProgressResponseDto {

    private List<UserDashboardTopicProgressItemDto> items;
    
    private int offset;
    
    private int pageSize;
    
    private long total;
}


