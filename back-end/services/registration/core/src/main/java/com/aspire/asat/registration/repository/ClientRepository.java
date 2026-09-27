package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends MongoRepository<Client, UUID> {

    Optional<Client> findById(UUID id);

    boolean existsById(UUID id);

    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'email': { $regex: ?0, $options: 'i' } } ] }")
    List<Client> findByTextSearch(String text);

}