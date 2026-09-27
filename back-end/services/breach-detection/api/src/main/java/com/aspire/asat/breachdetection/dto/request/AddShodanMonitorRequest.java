package com.aspire.asat.breachdetection.dto.request;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddShodanMonitorRequest {

    @NotNull
    private ShodanSubjectType subjectType;

    /** IP address (e.g. {@code 8.8.8.8}) or domain (e.g. {@code example.com}). */
    @NotBlank
    @Size(max = 253)
    private String subject;

    @Size(max = 500)
    private String notes;
}
