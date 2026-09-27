package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProductOverviewWrapperDTO {

    private int availableProducts; // Total number of products assigned
    private List<ClientProductOverviewResponseDTO> products;
}
