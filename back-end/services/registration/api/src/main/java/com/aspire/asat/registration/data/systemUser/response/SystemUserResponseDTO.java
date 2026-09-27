package com.aspire.asat.registration.data.systemUser.response;

import com.aspire.asat.registration.data.enums.RiskGroup;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SystemUserResponseDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String designation;
    private String department;
    private String country;
    private String zipCode;
    private String supervisorName;
    private String status;
    private RiskGroup riskGroup;
    private List<String> roles; // Role names
    private Instant createdAt;
    private Instant lastLoginAt;
}
