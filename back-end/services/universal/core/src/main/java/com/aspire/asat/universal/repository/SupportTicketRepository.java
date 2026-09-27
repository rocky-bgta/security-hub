package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.supportTicket.enums.Priority;
import com.aspire.asat.universal.supportTicket.enums.SupportType;
import com.aspire.asat.universal.supportTicket.enums.TicketStatus;
import com.aspire.asat.universal.entity.SupportTicket;
import com.aspire.asat.universal.repository.custom.SupportTicketRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends MongoRepository<SupportTicket, String>, SupportTicketRepositoryCustom {

    Optional<SupportTicket> findById(String id);

    Optional<SupportTicket> findByTicketId(String ticketId);

    List<SupportTicket> findByClientId(String clientId);

    Page<SupportTicket> findByClientId(String clientId, Pageable pageable);

    Page<SupportTicket> findByAssignedTo(String assignedTo, Pageable pageable);

    Page<SupportTicket> findByStatus(TicketStatus status, Pageable pageable);

    Page<SupportTicket> findByPriority(Priority priority, Pageable pageable);

    Page<SupportTicket> findBySupportType(SupportType supportType, Pageable pageable);

    Page<SupportTicket> findByMspId(String mspId, Pageable pageable);

    @Query("{'clientId': ?0, 'status': ?1}")
    Page<SupportTicket> findByClientIdAndStatus(String clientId, TicketStatus status, Pageable pageable);

    @Query("{'clientId': ?0, 'priority': ?1}")
    Page<SupportTicket> findByClientIdAndPriority(String clientId, Priority priority, Pageable pageable);

    @Query("{'clientId': ?0, 'supportType': ?1}")
    Page<SupportTicket> findByClientIdAndSupportType(String clientId, SupportType supportType, Pageable pageable);

    @Query("{'assignedTo': ?0, 'status': ?1}")
    Page<SupportTicket> findByAssignedToAndStatus(String assignedTo, TicketStatus status, Pageable pageable);

    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    Page<SupportTicket> findByTitleContainingIgnoreCase(String searchTerm, Pageable pageable);

    @Query("{'description': {$regex: ?0, $options: 'i'}}")
    Page<SupportTicket> findByDescriptionContainingIgnoreCase(String searchTerm, Pageable pageable);

    @Query("{'$or': [{'title': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    Page<SupportTicket> findByTitleOrDescriptionContainingIgnoreCase(String searchTerm, Pageable pageable);

    long countByClientId(String clientId);

    long countByStatus(TicketStatus status);

    long countByClientIdAndStatus(String clientId, TicketStatus status);
}

