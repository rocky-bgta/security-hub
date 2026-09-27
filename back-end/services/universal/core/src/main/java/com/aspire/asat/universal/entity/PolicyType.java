package com.aspire.asat.universal.entity;

import com.aspire.asat.universal.enums.PolicyTypeCode;
import com.aspire.asat.universal.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "policy_types")
public class PolicyType {

    @Id
    private String id;
    private String name;
    private PolicyTypeCode code;
    private Status status;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

