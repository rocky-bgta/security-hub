package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.PackagesDto;

import java.util.List;
import java.util.UUID;

public interface PackagesService {

    ApiResponse<List<PackagesDto>> getAllPackages(Integer offset, Integer pageSize);

    PackagesDto save(PackagesDto savePackagesDto);

    PackagesDto getPackagesById(UUID id);

    PackagesDto updatePackages(PackagesDto toBeUpdate);

}
