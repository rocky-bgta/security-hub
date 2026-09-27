package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.common.BulkStatusUpdateRequestDto;
import com.aspire.asat.cms.dto.common.ListOfUUID;
import com.aspire.asat.cms.dto.product.AssignedProductTagDto;
import com.aspire.asat.cms.dto.product.ProductCreationRequest;
import com.aspire.asat.cms.dto.product.ProductPackageSimpleResponse;
import com.aspire.asat.cms.dto.product.ProductResponse;
import com.aspire.asat.cms.dto.product.ProductWithSinglePackageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@RequestMapping(value = WebApiUrlConstants.PRODUCT_API, produces = "application/json")
@Tag(name = "Product Management", description = "APIs for managing products")
public interface ProductController {

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details.")
    ResponseEntity<ApiResponseDto<ProductResponse>> createProduct(@Valid @RequestBody ProductCreationRequest requestDto);

    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ProductResponse>>>> getAllProducts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ENABLED)
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ProductResponse>>>> getAllProductsByStatus(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ProductResponse>> getProductById(@PathVariable String id);

    @GetMapping("/package")
    @Operation(summary = "Get product with specific package", description = "Retrieves a product with a specific package by productId and packageId")
    ResponseEntity<ApiResponseDto<ProductWithSinglePackageResponse>> getProductPackageDetails(
            @RequestParam(value = "productId") String productId,
            @RequestParam(value = "packageId") String packageId);

    @PostMapping("/packages/by-ids")
    @Operation(
            summary = "Get product packages by IDs",
            description = "Retrieves a minimal list of product packages (id and name) for the given package IDs"
    )
    ResponseEntity<ApiResponseDto<List<ProductPackageSimpleResponse>>> getPackagesByIds(
            @Parameter(description = "List of package IDs", required = true)
            @Valid @RequestBody ListOfUUID requestDto
    );

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ProductResponse>> updateProductById(@PathVariable("id") String id, @Valid @RequestBody ProductCreationRequest updateRequestDto);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<String>> deleteProductById(@PathVariable String id);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_EXPORT, produces = "text/csv")
    void exportProductsToCsv(HttpServletResponse response);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_BULK_DELETE)
    ResponseEntity<ApiResponseDto<List<String>>> deleteProductsByIds(@Valid @RequestBody ListOfUUID requestDto);

    @PutMapping(WebApiUrlConstants.PATH_VAR_BULK_UPDATE)
    ResponseEntity<ApiResponseDto<List<String>>> bulkUpdateProductsStatus(@RequestBody BulkStatusUpdateRequestDto requestDto);

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_BULK_EXPORT, produces = "text/csv")
    void exportBulkProductsToCsv(@Valid @RequestBody ListOfUUID requestDto, HttpServletResponse response);


    //assign product to client admin

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ASSIGN)
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ProductResponse>>>> getAllAssignedProducts(
            @RequestParam(value = "clientAdminId") String clientAdminId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ASSIGN + "/tags")
    ResponseEntity<ApiResponseDto<List<AssignedProductTagDto>>> getAssignedProductTags(
            @RequestParam(value = "clientAdminId") String clientAdminId);

    @GetMapping("/trial")
    @Operation(summary = "Get all trial products", description = "Retrieves a paginated list of all trial products with optional search and filtering.")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ProductResponse>>>> getAllTrialProducts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order);



}
