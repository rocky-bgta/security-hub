package com.aspire.asat.registration.data.microsoft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportErrorDto {
    private String userEmail;
    private String errorMessage;
    private String errorCode;
}

