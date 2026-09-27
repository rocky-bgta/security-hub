package com.aspire.asat.registration.data.microsoft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportUsersResponseDto {
    private int totalUsersProcessed;
    private int usersCreated;
    private int usersSkipped;
    private int usersFailed;
    private Instant completedAt;
}

