package com.aspire.asat.universal.data.externalresponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubPackageDetailResponseDto {
    private String id;
    private String name;
    private String description;
    private String clientId;
    private String clientAdminId;
    private String createdBy;
    private String status;
    private String createdAt;
    private String updatedAt;
    private Long assignedUserCount;
    private ProductDetailsDto productDetails;
    private Object packageDetails; // Can be null
    private List<Object> topicDetails; // List of topic details
}

