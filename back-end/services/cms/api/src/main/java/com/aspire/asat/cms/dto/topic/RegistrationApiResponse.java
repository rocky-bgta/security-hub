package com.aspire.asat.cms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper DTO to match Registration service's ApiResponse structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationApiResponse<T> {
    private String message;
    private int statusCode;
    private T data;
}
