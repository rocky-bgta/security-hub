package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.PackagesController;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.PackagesDto;
import com.aspire.asat.registration.service.PackagesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class PackagesControllerImpl implements PackagesController {

    private final PackagesService packagesService;

    public PackagesControllerImpl(PackagesService packagesService) {
        this.packagesService = packagesService;
    }

    @Override
    public ResponseEntity<PackagesDto> createPackages(@RequestBody PackagesDto savePackagesDto) {
        PackagesDto savedPackages = packagesService.save(savePackagesDto);
        return new ResponseEntity<>(savedPackages, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponse<List<PackagesDto>>> getAllPackages(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize) {
        ApiResponse<List<PackagesDto>> packages = packagesService.getAllPackages(offset, pageSize);
        return new ResponseEntity<>(packages, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PackagesDto> getPackagesById(@PathVariable("id") UUID id) {
        PackagesDto packagesDto = packagesService.getPackagesById(id);
        return new ResponseEntity<>(packagesDto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PackagesDto> updatePackages(@RequestBody PackagesDto toBeUpdate) {
        PackagesDto packagesDtoFromDb = packagesService.updatePackages(toBeUpdate);
        return new ResponseEntity<>(packagesDtoFromDb, HttpStatus.OK);
    }

}
