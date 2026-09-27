package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.PackagesDto;
import com.aspire.asat.registration.exception.PackagesDoesNotExistException;
import com.aspire.asat.registration.model.Packages;
import com.aspire.asat.registration.repository.PackagesRepository;
import com.aspire.asat.registration.service.PackagesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PackagesServiceImpl implements PackagesService {

    public static final String PACKAGE_IS_NULL = "Package is null";
    private final PackagesRepository packagesRepository;

    public PackagesServiceImpl(PackagesRepository packagesRepository) {
        this.packagesRepository = packagesRepository;
    }

    @Override
    public ApiResponse<List<PackagesDto>> getAllPackages(Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Packages> pagePackages = packagesRepository.findAll(pageable);
        List<Packages> allPackages = pagePackages.getContent();
        List<PackagesDto> packagesDtos = allPackages.stream().map(packages -> packages.toPackagesDto(packages)).collect(Collectors.toList());
        return new ApiResponse<>("Data fetched successfully", HttpStatus.OK.value(), packagesDtos);
    }

    @Override
    public PackagesDto save(PackagesDto savePackagesDto) {
        Packages packages = packagesRepository.save(Packages.toPackages(savePackagesDto));
        return packages.toPackagesDto(packages);
    }

    @Override
    public PackagesDto getPackagesById(UUID id) {
        Optional<Packages> packagesFromDb = packagesRepository.findById(id);
        if (packagesFromDb.isEmpty()) {
            throw new PackagesDoesNotExistException(PACKAGE_IS_NULL);
        }
        return Packages.toPackagesDto(packagesFromDb.get());
    }

    @Override
    public PackagesDto updatePackages(PackagesDto toBeUpdate) {
        if (!packagesRepository.existsById(toBeUpdate.getId())) {
            throw new PackagesDoesNotExistException(PACKAGE_IS_NULL);
        }
        Packages updatedPackages = packagesRepository.save(Packages.toUpdatePackages(toBeUpdate));
        return Packages.toPackagesDto(updatedPackages);
    }

}
