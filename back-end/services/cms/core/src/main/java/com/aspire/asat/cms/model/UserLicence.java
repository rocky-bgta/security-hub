package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.enums.LicenceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_licence")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "user_licence_idx", def = "{'userId': 1, 'packageId': 1}", unique = true)
public class UserLicence {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String clientAdminId;

    @Indexed
    private String productId;

    @Indexed
    private String packageId;

    @Indexed
    private String subPackageId;

    private LicenceStatus licenceStatus;

    private Instant issueDate;

    private Instant expireDate;
}
