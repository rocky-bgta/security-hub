package com.aspire.asat.registration.data.metadata.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationTypeList {
    private String id;
    private String organizationType;
}
