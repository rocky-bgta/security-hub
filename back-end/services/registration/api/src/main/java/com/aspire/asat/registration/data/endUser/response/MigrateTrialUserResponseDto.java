package com.aspire.asat.registration.data.endUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after successful trial user migration")
public class MigrateTrialUserResponseDto {

    @Schema(description = "Original trial client admin ID that was migrated", example = "old-trial-client-admin-id")
    private String oldClientAdminId;

    @Schema(description = "New client admin ID (regular account)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String newClientAdminId;

    @Schema(description = "Number of users migrated from trial account", example = "5")
    private Integer migratedUsersCount;

    @Schema(description = "Success message", example = "Trial user account migrated successfully")
    private String message;
}
