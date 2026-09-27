package com.aspire.asat.registration.data.microsoft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDto {
    private String importId;
    private String status;
    private int totalUsersProcessed;
    private int usersCreated;
    private int usersUpdated;
    private int usersSkipped;
    private int usersFailed;
    private List<ImportErrorDto> errors;
    private Instant startedAt;
    private Instant completedAt;
}

