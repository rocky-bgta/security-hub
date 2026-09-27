package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "client_product_replica")
@CompoundIndex(name = "client_product_replica_idx", def = "{'clientAdminId': 1, 'productId': 1, 'packageId': 1}", unique = true)
public class ClientProductReplica {

    @Id
    private String id;

    @Indexed
    private String clientAdminId;

    @Indexed
    private String productId;

    @Indexed
    private String packageId;

    private Instant assignedAt;

    private Instant expiryDate;

    private String email;

    private Instant createdAt;

    private Instant updatedAt;
}

