package com.aspire.asat.cms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated topics for product/package pairs, with both assigned and locked totals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicsByProductPackagesPageDto {

    private int offset;
    private int pageSize;
    /** Topics matching the product/package pairs (assigned). */
    private long total;
    /** Topics not matching any pair (locked / unassigned). */
    private long totalLocked;
    private List<TopicMinimalDto> items;
}
