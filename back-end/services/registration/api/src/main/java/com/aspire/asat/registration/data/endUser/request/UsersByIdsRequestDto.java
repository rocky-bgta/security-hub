package com.aspire.asat.registration.data.endUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to fetch AspireUser details by a list of user IDs")
public class UsersByIdsRequestDto {

    @NotEmpty(message = "userIds must not be empty")
    @Schema(description = "List of user IDs", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> userIds;
}
