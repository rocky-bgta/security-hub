package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.SupportTicketType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketTypeRepository extends MongoRepository<SupportTicketType, String>, SupportTicketTypeRepositoryCustom {

    Optional<SupportTicketType> findByIdAndActiveTrue(String id);

    List<SupportTicketType> findByActiveTrue();

}
