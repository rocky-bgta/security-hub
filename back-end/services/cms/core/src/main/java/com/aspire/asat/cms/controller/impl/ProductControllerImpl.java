package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.cms.controller.ProductController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.common.BulkStatusUpdateRequestDto;
import com.aspire.asat.cms.dto.common.ListOfUUID;
import com.aspire.asat.cms.dto.product.AssignedProductTagDto;
import com.aspire.asat.cms.dto.product.ProductCreationRequest;
import com.aspire.asat.cms.dto.product.ProductPackageSimpleResponse;
import com.aspire.asat.cms.dto.product.ProductResponse;
import com.aspire.asat.cms.dto.product.ProductWithSinglePackageResponse;
import com.aspire.asat.cms.service.ProductService;
import com.aspire.asat.common.enums.ActivityType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductControllerImpl implements ProductController {
    private final ProductService productService;

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created product: #{#requestDto.name != null ? #requestDto.name : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<ProductResponse>> createProduct(@Valid @RequestBody ProductCreationRequest requestDto) {
        ProductResponse savedProduct = productService.saveProduct(requestDto);
        ApiResponseDto<ProductResponse> response = new ApiResponseDto<>("Product created successfully", HttpStatus.CREATED.value(), savedProduct);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ProductResponse>>>> getAllProducts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order) {
        List<ProductResponse> responseDtos = productService.getAllProducts(search, status, offset, pageSize, sortBy, order);
        AllResponseDto<List<ProductResponse>> allResponseDto = new AllResponseDto<>(offset, pageSize, productService.getTotalProductCount(), responseDtos);
        ApiResponseDto<AllResponseDto<List<ProductResponse>>> response = new ApiResponseDto<>("Products retrieved successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ProductResponse>>>> getAllProductsByStatus(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order) {
        List<ProductResponse> responseDtos = productService.getAllProductsByStatus(search, offset, pageSize, sortBy, order);
        AllResponseDto<List<ProductResponse>> allResponseDto = new AllResponseDto<>(offset, pageSize, productService.getTotalProductCount(), responseDtos);
        ApiResponseDto<AllResponseDto<List<ProductResponse>>> response = new ApiResponseDto<>("Products retrieved successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProductResponse>> getProductById(@PathVariable String id) {
        ProductResponse responseDto = productService.getProductById(id);
        ApiResponseDto<ProductResponse> response = new ApiResponseDto<>("Product retrieved successfully", HttpStatus.OK.value(), responseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ProductWithSinglePackageResponse>> getProductPackageDetails(
            @RequestParam(value = "productId") String productId,
            @RequestParam(value = "packageId") String packageId) {
        ProductWithSinglePackageResponse responseDto = productService.getProductPackageDetails(productId, packageId);
        ApiResponseDto<ProductWithSinglePackageResponse> response = new ApiResponseDto<>("Product retrieved successfully", HttpStatus.OK.value(), responseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ProductPackageSimpleResponse>>> getPackagesByIds(
            @Valid @RequestBody ListOfUUID requestDto) {
        List<ProductPackageSimpleResponse> packages = productService.getPackagesByIds(requestDto.getIds());
        ApiResponseDto<List<ProductPackageSimpleResponse>> response = new ApiResponseDto<>(
                "Packages retrieved successfully", HttpStatus.OK.value(), packages);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated product: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#updateRequestDto.name != null ? #updateRequestDto.name : #id}"
    )
    public ResponseEntity<ApiResponseDto<ProductResponse>> updateProductById(@PathVariable("id") String id, @Valid @RequestBody ProductCreationRequest updateRequestDto) {
        ProductResponse updatedProduct = productService.updateProductById(id, updateRequestDto);
        ApiResponseDto<ProductResponse> response = new ApiResponseDto<>("Product updated successfully", HttpStatus.OK.value(), updatedProduct);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteProductById(@PathVariable String id) {
        String deletedProductName = productService.deleteProductById(id);
        ApiResponseDto<String> response = new ApiResponseDto<>(
                "Product deleted successfully", HttpStatus.OK.value(), "Deleted Product: " + deletedProductName
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public void exportProductsToCsv(HttpServletResponse response) {
        productService.exportProducts(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> deleteProductsByIds(@Valid @RequestBody ListOfUUID requestDto) {
        List<String> deletedProductNames = productService.deleteProductsByIds(requestDto.getIds());
        ApiResponseDto<List<String>> response = new ApiResponseDto<>("Products deleted successfully", HttpStatus.OK.value(), deletedProductNames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> bulkUpdateProductsStatus(@RequestBody BulkStatusUpdateRequestDto requestDto) {
        List<String> updatedProductNames = productService.updateProductsStatusByIds(
                requestDto.getIds(),
                requestDto.getStatus()
        );
        ApiResponseDto<List<String>> response = new ApiResponseDto<>(
                "Products updated successfully", HttpStatus.OK.value(), updatedProductNames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public void exportBulkProductsToCsv(@RequestBody ListOfUUID requestDto, HttpServletResponse response) {
        productService.exportBulkProducts(requestDto.getIds(), response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ProductResponse>>>> getAllAssignedProducts(String clientAdminId, String search, Integer offset, Integer pageSize, String sortBy, String order) {
        List<ProductResponse> responseDtos = productService.getAllAssignedProducts(clientAdminId, search, offset, pageSize, sortBy, order);
        long totalCount = productService.getTotalAssignedProductCount(clientAdminId, search);
        AllResponseDto<List<ProductResponse>> allResponseDto = new AllResponseDto<>(offset, pageSize, totalCount, responseDtos);
        ApiResponseDto<AllResponseDto<List<ProductResponse>>> response = new ApiResponseDto<>("Assigned products retrieved successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<AssignedProductTagDto>>> getAssignedProductTags(String clientAdminId) {
        List<AssignedProductTagDto> responseDtos = productService.getAssignedProductTags(clientAdminId);
        ApiResponseDto<List<AssignedProductTagDto>> response = new ApiResponseDto<>("Assigned product tags retrieved successfully", HttpStatus.OK.value(), responseDtos);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ProductResponse>>>> getAllTrialProducts(
            String search, String status, Integer offset, Integer pageSize, String sortBy, String order) {
        List<ProductResponse> responseDtos = productService.getAllTrialProducts(search, status, offset, pageSize, sortBy, order);
        long totalCount = productService.getTotalTrialProductCount(search, status);
        AllResponseDto<List<ProductResponse>> allResponseDto = new AllResponseDto<>(offset, pageSize, totalCount, responseDtos);
        ApiResponseDto<AllResponseDto<List<ProductResponse>>> response = new ApiResponseDto<>("Trial products retrieved successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
