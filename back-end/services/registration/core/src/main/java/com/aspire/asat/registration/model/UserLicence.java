package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.enums.LicenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_licence")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLicence {

    private String id;
    private String userId;
    private String clientAdminId;
    private String productId;
    private String packageId;
    private String subPackageId;
    private LicenceStatus licenceStatus;
    private Instant issueDate;
    private Instant expireDate;
}
