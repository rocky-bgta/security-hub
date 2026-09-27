package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.ClientMicroContentTopic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientMicroContentTopicRepository extends MongoRepository<ClientMicroContentTopic, String> {

    Optional<ClientMicroContentTopic> findByClientId(String clientId);
}
