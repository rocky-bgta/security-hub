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
@Document(collection = "user_certificates")
@CompoundIndex(name = "user_certificate_idx", def = "{'userId': 1, 'userPackageId': 1}", unique = true)
public class UserCertificate {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String examId;

    private String certificateId;

    @Indexed
    private String userSubPackageId;

    @Indexed
    private String clientAdminId;

    private String subPackageId;

    private String username; // save email here
    private String fullName;
    private String productName;
    private String certificateUrl;
    private String certificateImageUrl;
    private Instant expiryDate;
    private Instant createdAt;
    private String status;

    private String subPackageName;
    private String certificateLink;
    private String imageCertificateLink;
}
