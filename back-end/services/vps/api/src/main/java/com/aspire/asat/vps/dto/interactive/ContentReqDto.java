package com.aspire.asat.vps.dto.interactive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReqDto<T> {

    private CommonContent common;
    private T specific;
}
