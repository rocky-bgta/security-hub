package com.aspire.asat.registration.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor

public class LicenseCountDto {

    private String name;
    private long totalLicense;
    private long onBoarded;
    private long expired;
    private long toBeExpired;

}
