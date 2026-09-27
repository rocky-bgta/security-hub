package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AvailablePackageWrapperDTO {
    private int total;
    private List<AvailablePackageDTO> packages;
}
