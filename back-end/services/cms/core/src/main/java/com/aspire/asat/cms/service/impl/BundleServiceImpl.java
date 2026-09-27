package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.bundle.BundleDto;
import com.aspire.asat.cms.model.Bundle;
import com.aspire.asat.cms.repository.BundleRepository;
import com.aspire.asat.cms.service.BundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BundleServiceImpl implements BundleService {

    @Autowired
    private BundleRepository bundleRepository;

//    // Get Bundle by ID and map to ApiResponseDto
//    public ApiResponseDto<Bundle> getBundleById(String id) {
//        List<Bundle> resbundle = bundleRepository.findByBundleId(id);
//        Bundle bundle = resbundle.get(0);
//        if (bundle == null) {
//            return new ApiResponseDto<>("Bundle not found", HttpStatus.NOT_FOUND.value(), null);
//        }
//
//        return new ApiResponseDto<>("Bundle found successfully", HttpStatus.OK.value(), bundle);
//    }
//
//    // Get list of Bundles with pagination, sorting, and text search on bundleName
//    public AllResponseDto<List<Bundle>> getBundles(String bundleName, int page, int size, boolean sortByLatest) {
//        // Check if bundleName is null or empty
//        if (bundleName == null || bundleName.isEmpty()) {
//            bundleName = ""; // fallback to an empty string for regex search
//        }
//
//        // Use PageRequest for pagination and sorting
//        Sort sort = sortByLatest ? Sort.by(Sort.Order.desc("createdAt")) : Sort.by(Sort.Order.asc("createdAt"));
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        // Retrieve the page of bundles, using regex search for bundleName
//        Page<Bundle> bundlesPage = bundleRepository.findByBundleNameContaining(bundleName, pageable);
//
//        // Only return the items (bundles) without the pagination metadata
//        return new AllResponseDto<>(page, size, bundlesPage.getTotalElements(), bundlesPage.getContent());
//    }
}