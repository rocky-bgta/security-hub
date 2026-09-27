package com.aspire.asat.billing.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "clients")
@Data
public class ClientInfo {
    @Id
    private String id;
    private String clientName;
    private String contactEmail;
    private String phone;
    private String businessName;
    private String address;
    private Instant createdAt = Instant.now();
}
