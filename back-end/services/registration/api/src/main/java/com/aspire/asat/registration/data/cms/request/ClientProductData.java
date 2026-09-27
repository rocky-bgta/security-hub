package com.aspire.asat.registration.data.cms.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProductData {
    private String clientAdminId;
    private String productId;
    private String packageId;
    private Instant assignedAt;
    private Instant expiryDate;
    private String email;
}

