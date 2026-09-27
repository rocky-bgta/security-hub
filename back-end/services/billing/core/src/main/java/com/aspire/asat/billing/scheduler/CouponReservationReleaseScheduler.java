package com.aspire.asat.billing.scheduler;

import com.aspire.asat.billing.service.CouponReservationReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponReservationReleaseScheduler {

    private final CouponReservationReleaseService couponReservationReleaseService;

    @Value("${billing.coupon.expiry-cancel.enabled:true}")
    private boolean cancelEnabled;

    @Scheduled(cron = "${billing.coupon.expiry-cancel.cron:0 0 * * * ?}")
    public void cancelExpiredCouponInvoices() {
        if (!cancelEnabled) {
            log.debug("Coupon expiry invoice cancel scheduler is disabled");
            return;
        }

        try {
            log.info("Starting coupon-expiry invoice cancel job");
            int cancelled = couponReservationReleaseService.cancelExpiredCouponInvoices();
            log.info("Completed coupon-expiry invoice cancel job. Cancelled: {}", cancelled);
        } catch (Exception e) {
            log.error("Error in coupon-expiry invoice cancel scheduler: {}", e.getMessage(), e);
        }
    }
}
