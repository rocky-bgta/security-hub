package com.aspire.asat.auth.model.token;

import com.aspire.asat.auth.dto.enums.PlatformType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceInfoRequest {

    @NotBlank
    @Schema(defaultValue = "WEB", requiredMode = Schema.RequiredMode.REQUIRED)
    private PlatformType platformType;

    @NotBlank
    @Schema(defaultValue = "GOOGLE CHROME", requiredMode = Schema.RequiredMode.REQUIRED)
    private String platformInfo;

    @NotBlank
    @Schema(defaultValue = "1001.0.1.1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String platformVersion;

    @NotBlank
    @Schema(defaultValue = "42345245", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceIdentifier;

    @NotBlank
    @Schema(defaultValue = "ENGLISH", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appLanguage;

    @NotBlank
    @Schema(defaultValue = "1.0.1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appVersion;
}
