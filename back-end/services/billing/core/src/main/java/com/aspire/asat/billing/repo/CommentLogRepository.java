package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.CommentLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentLogRepository extends MongoRepository<CommentLog, String> {

    List<CommentLog> findByInvoiceId(String invoiceId);

    List<CommentLog> findByInvoiceIdOrderByDateDesc(String invoiceId);
}

