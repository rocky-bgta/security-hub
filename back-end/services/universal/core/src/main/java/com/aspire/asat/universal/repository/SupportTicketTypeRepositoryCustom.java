package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.SupportTicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportTicketTypeRepositoryCustom {

    Page<SupportTicketType> findAllWithFilters(String search, Boolean active, Pageable pageable);
}

