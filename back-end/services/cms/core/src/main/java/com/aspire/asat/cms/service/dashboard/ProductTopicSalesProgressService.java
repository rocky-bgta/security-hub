package com.aspire.asat.cms.service.dashboard;

import com.aspire.asat.cms.dto.dashboard.ProductTopicSalesProgressResponseDto;
import com.aspire.asat.cms.dto.enums.TimeFrame;

/**
 * Service interface for product-topic sales progress analytics
 */
public interface ProductTopicSalesProgressService {
    
    /**
     * Get sales progress by product-topics according to timeFrame
     * - YEARLY: Returns year-wise data for previous 6 years (including current year)
     * - MONTHLY: Returns month-wise data for current year (Jan-Dec)
     * 
     * @param timeFrame YEARLY for year-wise data or MONTHLY for month-wise data (default: YEARLY)
     * @return ProductTopicSalesProgressResponseDto with chart data
     */
    ProductTopicSalesProgressResponseDto getProductTopicSalesProgress(TimeFrame timeFrame);

    /**
     * Get MSP-scoped sales progress by product-topics.
     * Resolves client admins for the MSP, then unique products from client_product_replica,
     * then reuses the same topic aggregation as organization sales-progress.
     *
     * @param timeFrame YEARLY or MONTHLY
     * @param mspId MSP ID (required)
     * @return ProductTopicSalesProgressResponseDto with chart data
     */
    ProductTopicSalesProgressResponseDto getMspProductTopicSalesProgress(TimeFrame timeFrame, String mspId);
}

