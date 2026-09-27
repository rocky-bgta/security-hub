package com.aspire.asat.breachdetection.model;

import com.aspire.asat.breachdetection.dto.enums.InsecureWebBreachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "insecureweb_breach_findings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "client_external_idx", def = "{'clientId':1,'externalFindingId':1}", unique = true),
        @CompoundIndex(name = "client_timestamp_idx", def = "{'clientId':1,'timestamp':-1}")
})
public class InsecureWebBreachFinding {
    @Id
    private String id;

    @Indexed
    private String clientId;

    @Indexed
    private Long organizationId;

    @Indexed
    private String externalFindingId;

    private Instant timestamp;
    private String domain;
    private String email;
    private String ipAddress;
    private String username;
    private String password;
    private String hashedPassword;
    private String phone;
    private String databaseName;
    private String foundIn;
    private String source;
    private String leakName;
    private String breachDescription;
    private String compromisedData;
    private String victimDomain;
    private String organizationElement;
    private String organizationElementType;
    @Field("breachStatus")
    private InsecureWebBreachStatus breachStatus;
    private Boolean employee;
    private Integer echoesCount;
    private String rawPayloadJson;

    private Instant firstSeenAt;
    private Instant lastSeenAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
