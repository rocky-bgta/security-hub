package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingDataCaptureDto {

    private long totalCalls;
    private long compromisedCount;
    private List<CallLogDto> callLogs;
    private long total;
    private int offset;
    private int pageSize;
}
