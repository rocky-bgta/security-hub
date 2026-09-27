package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.ClientInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClientInfoRepository extends MongoRepository<ClientInfo, String> {
}
