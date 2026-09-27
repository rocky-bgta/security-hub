package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteResponseDTO {

    private String id;                 // ID of the deleted resource
    private String collection;         // Collection or entity name (e.g., coupons, commissions)
    private String message;            // Human-readable confirmation
    private long deletedAtTimestamp;   // Epoch time of deletion (auto-populated)

    public static DeleteResponseDTO of(String id, String collection, String message) {
        return new DeleteResponseDTO(id, collection, message, System.currentTimeMillis());
    }
}
