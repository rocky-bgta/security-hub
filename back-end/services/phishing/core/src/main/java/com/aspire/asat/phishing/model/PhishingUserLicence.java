package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.RiskGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Unique phishing license consumption for one user under one ClientProduct assignment.
 * Uniqueness: {@code clientAdminId + productPackageId + userId}.
 */
@Document(collection = "phishing_user_licence")
@CompoundIndexes({
        @CompoundIndex(name = "client_product_package_user_unique",
                def = "{'clientAdminId': 1, 'productPackageId': 1, 'userId': 1}", unique = true),
        @CompoundIndex(name = "client_product_package_idx",
                def = "{'clientAdminId': 1, 'productPackageId': 1}"),
        @CompoundIndex(name = "user_client_idx",
                def = "{'userId': 1, 'clientAdminId': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhishingUserLicence {

    @Id
    private String id;

    /** ClientProduct.id (license assignment). */
    private String productPackageId;

    private String clientAdminId;

    /** Copied from ClientProduct.expiryDate at allocation time. */
    private Instant packageExpireDate;

    /** Purchased seat count from ClientProduct.licenseCount (denormalized). */
    private int licenceCount;

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String departmentId;
    private String departmentName;
    private String organizationName;
    private String organizationDomain;
    private String phoneNumber;
    private String countryName;

    @Builder.Default
    private List<String> groupIds = new ArrayList<>();

    private boolean active;
    private Boolean isRiskProfileExist;
    /** Snapshot of Registration AspireUser.riskGroup at allocation time. */
    private RiskGroup riskGroup;

    @CreatedDate
    private Instant createdAt;
}
