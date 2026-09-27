package com.aspire.asat.registration.repository.msp;

import com.aspire.asat.registration.model.msp.MspInvoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MSP Invoice operations
 */
@Repository
public interface MspInvoiceRepository extends MongoRepository<MspInvoice, String> {

    /**
     * Find all invoice by MSP ID
     */
    List<MspInvoice> findByMspId(String mspId);

    /**
     * Find MSP invoice(s) by document id and MSP id.
     */
    List<MspInvoice> findByIdAndMspId(String id, String mspId);

}

