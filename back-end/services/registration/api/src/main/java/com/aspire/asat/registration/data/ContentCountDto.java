package com.aspire.asat.registration.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor

public class ContentCountDto {

    private String name;
    private long value;

}
