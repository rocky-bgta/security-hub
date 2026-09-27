package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.model.AiModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiModelRepository extends MongoRepository<AiModel, String> {

    List<AiModel> findByProviderTypeOrderByNameAsc(AiProviderType providerType);

    List<AiModel> findAllByOrderByProviderTypeAscNameAsc();

    List<AiModel> findByProviderTypeAndIsDefaultTrue(AiProviderType providerType);
}
