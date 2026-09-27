package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepfakeStep5Request {

    @NotBlank
    private String script;

    private Map<String, String> variables;
}
