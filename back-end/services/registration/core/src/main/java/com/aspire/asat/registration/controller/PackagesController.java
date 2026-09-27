package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.PackagesDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.PACKAGES_API, produces = "application/json")
public interface PackagesController {

    @PostMapping
    ResponseEntity<PackagesDto> createPackages(PackagesDto savePackagesDto);

    @GetMapping
    ResponseEntity<ApiResponse<List<PackagesDto>>> getAllPackages(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<PackagesDto> getPackagesById(UUID id);

    @PutMapping
    ResponseEntity<PackagesDto> updatePackages(@RequestBody PackagesDto packagesDto);

}
