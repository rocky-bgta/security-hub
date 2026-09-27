package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.Action;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActionRepository extends MongoRepository<Action, String> {

    Optional<Action> findByName(String name);

    boolean existsByName(String name);
}

