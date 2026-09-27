package com.aspire.asat.registration.data.trial.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExistingUsersResponseDto {

    @Schema(description = "Whether existing users were found", example = "true")
    private Boolean hasExistingUsers;

    @Schema(description = "List of existing admins")
    private List<ExistingAdminUserInfoDto> existingAdmins;

    @Schema(description = "List of existing users")
    private List<ExistingAdminUserInfoDto> existingUsers;

    @Schema(description = "Message to display", example = "An account already exists for Islami Bank. Please contact support.")
    private String message;
}
