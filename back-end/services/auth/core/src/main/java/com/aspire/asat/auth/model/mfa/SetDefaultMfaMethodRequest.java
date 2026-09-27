package com.aspire.asat.auth.model.mfa;

import com.aspire.asat.auth.dto.enums.MfaMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetDefaultMfaMethodRequest {

    @NotNull(message = "Method is required")
    private MfaMethod method;
}
