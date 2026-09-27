package com.aspire.asat.registration.data.cms.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardRequestDto {
    private String clientAdminId;
    private Integer totalProduct;
    private Integer totalLicense;
    private Integer totalTopic;
    private Integer totalCertificate;
    private List<ClientProductData> clientProductData;
}
