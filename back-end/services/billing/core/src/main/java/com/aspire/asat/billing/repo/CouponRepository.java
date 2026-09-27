package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {

    // Find coupon by code
    Optional<Coupon> findByCode(String code);

    // Check if a coupon with this code already exists
    boolean existsByCode(String code);

    // (Optional) Retrieve only active and valid coupons
    Optional<Coupon> findByCodeAndIsActiveTrueAndValidFromBeforeAndValidUntilAfter(String code, Instant currentDate1, Instant currentDate2);

    Page<Coupon> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    int countByCodeContainingIgnoreCase(String code);


}
