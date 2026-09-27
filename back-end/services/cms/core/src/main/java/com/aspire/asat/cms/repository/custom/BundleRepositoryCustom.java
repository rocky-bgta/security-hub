package com.aspire.asat.cms.repository.custom;

import com.aspire.asat.cms.dto.clientAdmin.ClientProductDTO;
import org.bson.Document;

import java.util.List;

public interface BundleRepositoryCustom {
    List<Document> getEnrichedBundleDetails(List<ClientProductDTO> clientProductDTOS);

}
