package com.aspire.asat.breachdetection.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "insecureweb_organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsecureWebOrganization {
    @Id
    private String id;

    @Indexed(unique = true)
    private String clientId;

    @Indexed(unique = true)
    private Long insecureWebOrganizationId;

    private String orgName;
    private String orgDescription;

    @Builder.Default
    private List<String> domains = new ArrayList<>();

    @Builder.Default
    private List<String> emails = new ArrayList<>();

    @Builder.Default
    private List<String> users = new ArrayList<>();

    @Builder.Default
    private List<String> ips = new ArrayList<>();

    @Builder.Default
    private List<String> phones = new ArrayList<>();

    @Builder.Default
    private List<String> scanServices = new ArrayList<>();

    @Builder.Default
    private boolean syncEnabled = true;

    private Instant lastSyncAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
