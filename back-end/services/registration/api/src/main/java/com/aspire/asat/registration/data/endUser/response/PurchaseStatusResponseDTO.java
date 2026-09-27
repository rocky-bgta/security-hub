package com.aspire.asat.registration.data.endUser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseStatusResponseDTO {
    private boolean trailExists;
    private boolean buyNowExists;
    private boolean alreadyRegistered;
}
