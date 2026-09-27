package com.aspire.asat.registration.service;


import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.LicenseCountDto;
import com.aspire.asat.registration.data.LicenseDto;

import java.util.List;

public interface LicenseService {

    List<LicenseDto> getAllLicense(Integer offset, Integer pageSize);

    LicenseDto save(LicenseDto licenseDto);

    ApiResponse<List<LicenseCountDto>> getAllLicenseCount();

    LicenseDto updateLicense(LicenseDto toBeUpdate);

}
