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
public class ClientAdminActivityLogResponseDto {
    private List<ClientAdminActivityLogDto> activityLogs;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}

