package com.aspire.asat.registration.data.auth.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for Auth service forced logout by userId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequestDto {

    private String userId;
}
