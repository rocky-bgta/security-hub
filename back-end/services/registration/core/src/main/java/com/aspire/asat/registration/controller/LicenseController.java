package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.LicenseCountDto;
import com.aspire.asat.registration.data.LicenseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.aspire.asat.registration.constant.WebApiUrlConstants.LICENSE_API;

@RequestMapping(value = LICENSE_API, produces = "application/json")
public interface LicenseController {

    @PostMapping
    ResponseEntity<LicenseDto> createLicense(LicenseDto licenseDto);

    @GetMapping
    ResponseEntity<List<LicenseDto>> getAllLicense(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize);

    @GetMapping(WebApiUrlConstants.LICENSE_COUNT)
    ResponseEntity<ApiResponse<List<LicenseCountDto>>> getLicenseCount();

    @PutMapping
    ResponseEntity<LicenseDto> updateLicense(@RequestBody LicenseDto toBeUpdate);

}