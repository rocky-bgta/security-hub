package com.aspire.asat.cms.dto.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ContentRespDto<T> {

    private String id;
    private ContentCommonDto common;
    private T specific;
    private Instant createdAt;
    private Instant updatedAt;
}
