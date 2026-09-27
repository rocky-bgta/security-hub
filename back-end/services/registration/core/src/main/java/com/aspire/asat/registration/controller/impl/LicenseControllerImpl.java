package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.LicenseController;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.LicenseCountDto;
import com.aspire.asat.registration.data.LicenseDto;
import com.aspire.asat.registration.service.LicenseService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LicenseControllerImpl implements LicenseController {


    private final LicenseService licenseService;

    public LicenseControllerImpl(LicenseService licenseService, MongoTemplate mongoTemplate) {
        this.licenseService = licenseService;
    }

    @Override
    public ResponseEntity<LicenseDto> createLicense(@RequestBody LicenseDto licenseDto) {
        LicenseDto savedLicense = licenseService.save(licenseDto);
        return new ResponseEntity<>(savedLicense, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<LicenseDto>> getAllLicense(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize) {
        List<LicenseDto> license = licenseService.getAllLicense(offset, pageSize);
        return new ResponseEntity<>(license, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponse<List<LicenseCountDto>>> getLicenseCount() {
        ApiResponse<List<LicenseCountDto>> response = licenseService.getAllLicenseCount();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<LicenseDto> updateLicense(@RequestBody LicenseDto toBeUpdate) {
        LicenseDto licenseDtoFromDb = licenseService.updateLicense(toBeUpdate);
        return new ResponseEntity<>(licenseDtoFromDb, HttpStatus.OK);
    }

}