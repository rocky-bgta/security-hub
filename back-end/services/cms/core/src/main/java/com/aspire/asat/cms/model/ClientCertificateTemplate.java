package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "client_certificate_template")
public class ClientCertificateTemplate {

    @Id
    private String id;

    @Indexed
    private String templateId;

    @Indexed
    private String clientAdminId;

    private boolean active;

    private String createdBy;

    private Instant createdAt;

    private String updatedBy;

    private Instant updatedAt;
}

