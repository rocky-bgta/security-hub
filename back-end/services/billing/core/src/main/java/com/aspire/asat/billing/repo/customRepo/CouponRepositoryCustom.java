package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.model.Coupon;

import java.util.Optional;

public interface CouponRepositoryCustom {

    /**
     * Atomically increments totalUsed only when totalUsed &lt; usageLimit.
     *
     * @return the updated coupon if a slot was reserved, empty if limit reached
     */
    Optional<Coupon> incrementUsageIfBelowLimit(String couponId);

    /**
     * Atomically decrements totalUsed only when totalUsed &gt; 0.
     *
     * @return the updated coupon if decremented, empty if totalUsed was already 0
     */
    Optional<Coupon> decrementUsageIfAboveZero(String couponId);
}
