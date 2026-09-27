package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.dto.enums.ProductStatus;
import com.aspire.asat.cms.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    boolean existsByProductName(String productName);

    @Query("{ 'productName': { $regex: ?0, $options: 'i' } }")
    Page<Product> findByProductName(String text, Pageable pageable);

    Page<Product> findByProductStatus(ProductStatus status, Pageable pageable);

}
