package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "client_dashboard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboard {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clientAdminId;

    private Integer totalProduct;

    private Integer totalLicense;

    private Integer totalTopic;

    private Integer totalCertificate;

    private Instant createdAt;

    private Instant updatedAt;
}
