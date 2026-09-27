package com.aspire.asat.vps.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VpsContentRespDto<T> {
    private String id;
    private VpsContentCommonDto common;
    private T specific;
    private Instant createdAt;
    private Instant updatedAt;

}
