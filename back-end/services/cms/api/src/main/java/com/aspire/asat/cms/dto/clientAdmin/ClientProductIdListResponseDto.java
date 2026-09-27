package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ClientProductIdListResponseDto {
    private List<String> clientIds;
    private List<ClientProductDTO> clientProductDTOS;
}
