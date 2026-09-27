package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.bundle.BundleDto;
import com.aspire.asat.cms.model.Bundle;
import com.aspire.asat.cms.service.BundleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bundles")
public class BundleController {

    @Autowired
    private BundleService bundleService;

//    // Get Bundle by ID and return response in ApiResponseDto format
//    @GetMapping("/{id}")
//    public ApiResponseDto<Bundle> getBundleById(@PathVariable String id) {
//        return bundleService.getBundleById(id);
//    }
//
//    // Get Bundles with pagination, sorting and text search
//    @GetMapping
//    public AllResponseDto<List<Bundle>> getBundles(
//            @RequestParam(required = false, defaultValue = "") String bundleName,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "true") boolean sortByLatest) {
//
//        // Get bundles wrapped in AllResponseDto
//        return bundleService.getBundles(bundleName, page, size, sortByLatest);
//    }
//
//    // Delete Bundle by ID
//    @DeleteMapping("/{id}")
//    public String deleteBundleById(@PathVariable String id) {
//        boolean isDeleted = bundleService.deleteBundleById(id);
//        if (isDeleted) {
//            return "Bundle with ID " + id + " deleted successfully";
//        } else {
//            return "Bundle with ID " + id + " not found";
//        }
//    }
}
