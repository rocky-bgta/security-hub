package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated MSP product catalog merged with assignment status")
public class MspProductCatalogResponseDto {

    private String mspId;
    private Integer offset;
    private Integer pageSize;
    private Long totalCount;
    private List<MspProductCatalogItemDto> items;
}
