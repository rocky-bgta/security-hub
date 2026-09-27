package com.aspire.asat.registration.data.clientAdmin.response;

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
