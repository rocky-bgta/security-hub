package com.aspire.asat.common.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordValidationResult {
    private boolean valid;
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public static PasswordValidationResult success() {
        return PasswordValidationResult.builder().valid(true).errors(Collections.emptyList()).build();
    }

    public static PasswordValidationResult failure(List<String> errors) {
        return PasswordValidationResult.builder().valid(false).errors(errors != null ? errors : new ArrayList<>()).build();
    }

    public String getCombinedMessage() {
        return errors == null || errors.isEmpty() ? "" : String.join(" ", errors);
    }
}
