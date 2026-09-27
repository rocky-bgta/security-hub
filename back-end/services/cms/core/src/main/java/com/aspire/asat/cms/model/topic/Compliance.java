package com.aspire.asat.cms.model.topic;

import com.aspire.asat.cms.dto.topic.ComplianceReqDto;
import com.aspire.asat.cms.dto.topic.ComplianceRespDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "compliance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Compliance {
    private String id;
    private String complianceName;
    private String acronym; // PCI, HIPAA, GDPR, FedRAMP
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;

    public static Compliance toCompliance(ComplianceReqDto complianceReqDto) {
        return Compliance.builder()
                .id(UUID.randomUUID().toString())
                .complianceName(complianceReqDto.getComplianceName())
                .acronym(complianceReqDto.getAcronym())
                .description(complianceReqDto.getDescription())
                .isActive(true) // Default to true when creating a new compliance
                .sortOrder(complianceReqDto.getSortOrder())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static ComplianceRespDto toComplianceRespDto(Compliance compliance) {
        return ComplianceRespDto.builder()
                .id(compliance.getId())
                .complianceName(compliance.getComplianceName())
                .acronym(compliance.getAcronym())
                .description(compliance.getDescription())
                .isActive(compliance.getIsActive())
                .sortOrder(compliance.getSortOrder())
                .createdAt(compliance.getCreatedAt())
                .updatedAt(compliance.getUpdatedAt())
                .build();
    }
}
