package com.aspire.asat.auth.model.token;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for user logout")
public class LogoutRequest {

    @Schema(description = "Device information for logout activity log", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private DeviceInfoRequest deviceInfo;
}
