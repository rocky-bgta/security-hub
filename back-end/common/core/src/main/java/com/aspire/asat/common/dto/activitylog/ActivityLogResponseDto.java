package com.aspire.asat.common.dto.activitylog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponseDto {
    private List<ActivityLogDto> activityLogs;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}

