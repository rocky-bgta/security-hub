package com.aspire.asat.auth.model.token;

import com.aspire.asat.auth.serializer.LowercaseSerializer;
import com.aspire.asat.auth.serializer.SensitiveDataSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginWithPasswordRequest {
    @NotBlank(message = "please.provide.username")
    @Schema(defaultValue = "dummy", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(converter = LowercaseSerializer.class)
    @JsonDeserialize(converter = LowercaseSerializer.class)
    private String username;

    @NotBlank(message = "please.provide.password")
    @Schema(defaultValue = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = SensitiveDataSerializer.class)
    private String password;

    @NotNull
    private DeviceInfoRequest deviceInfo;
}
