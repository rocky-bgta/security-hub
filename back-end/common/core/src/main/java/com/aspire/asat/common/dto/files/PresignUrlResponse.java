package com.aspire.asat.common.dto.files;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class PresignUrlResponse {
    private String url;
    private String message;
}