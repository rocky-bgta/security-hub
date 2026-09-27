package com.aspire.asat.registration.data.mspUser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspUpdateResponseDto {
    private String id;
    private String mspId;
    private String organizationName;
    private String status;
    private Instant updatedAt;
    private List<String> updatedFields;
    private List<String> updatedProductIds;
    private List<String> assignedClientIds;
    private List<String> removedClientIds;
    private boolean creditUpdated;
    private String message;
}
