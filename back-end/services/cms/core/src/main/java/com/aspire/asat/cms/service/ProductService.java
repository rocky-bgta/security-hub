package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.product.AssignedProductTagDto;
import com.aspire.asat.cms.dto.product.ProductCreationRequest;
import com.aspire.asat.cms.dto.product.ProductPackageSimpleResponse;
import com.aspire.asat.cms.dto.product.ProductResponse;
import com.aspire.asat.cms.dto.product.ProductWithSinglePackageResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.List;

public interface ProductService {

    ProductResponse saveProduct(ProductCreationRequest requestDto);

    List<ProductResponse> getAllProducts(String search, String status, Integer offset, Integer pageSize, String sortBy, String order);

    ProductResponse getProductById(String id);

    ProductWithSinglePackageResponse getProductPackageDetails(String productId, String packageId);

    ProductResponse updateProductById(String ProductId, ProductCreationRequest updateRequestDto);

    String deleteProductById(String id);

    void exportProducts(HttpServletResponse response);

    long getTotalProductCount();

    List<String> deleteProductsByIds(List<String> ids);

    List<String> updateProductsStatusByIds(List<String> ids, Status status);

    void exportBulkProducts(List<String> ids, HttpServletResponse response);

    List<ProductResponse> getAllProductsByStatus(String search, Integer offset, Integer pageSize, String sortBy, String order);

    List<ProductResponse> getAllAssignedProducts(String clientAdminId, String search,
                                                 Integer offset, Integer pageSize,
                                                 String sortBy, String order);

    long getTotalAssignedProductCount(String clientAdminId, String search);

    List<AssignedProductTagDto> getAssignedProductTags(String clientAdminId);

    List<ProductResponse> getAllTrialProducts(String search, String status, Integer offset, Integer pageSize, String sortBy, String order);

    long getTotalTrialProductCount(String search, String status);

    /**
     * Fetch product packages by their IDs (minimal id + name).
     *
     * @param packageIds package IDs to resolve
     * @return matching packages with id and name
     */
    List<ProductPackageSimpleResponse> getPackagesByIds(Collection<String> packageIds);
}
