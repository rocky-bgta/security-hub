package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProductOverviewResponseDTO {
    private String productId;
    private String title;
    private String description;
    private int enrolledUsers;
    private int completionRate;
    private String thumbnailUrl;
    private boolean active;
}
