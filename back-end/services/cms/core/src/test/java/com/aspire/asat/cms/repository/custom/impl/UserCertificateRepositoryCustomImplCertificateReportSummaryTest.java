package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.repository.ProductRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCertificateRepositoryCustomImplCertificateReportSummaryTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UserCertificateRepositoryCustomImpl repository;

    @Test
    void getCertificateReportSummaryStats_allFiltersNull_returnsZerosWithoutNpe() {
        AggregationResults<Document> results = mock(AggregationResults.class);
        when(results.getUniqueMappedResult()).thenReturn(null);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("user_certificates"), eq(Document.class)))
                .thenReturn(results);

        ExpiredCertificateReportSummaryDTO summary = repository.getCertificateReportSummaryStats(
                null, null, null, null, null);

        assertEquals(0L, summary.getTotalCertificates());
        assertEquals(0L, summary.getTotalValidCertificates());
        assertEquals(0L, summary.getTotalExpiredCertificates());
        assertEquals(0L, summary.getTotalExpiringCertificates());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("user_certificates"), eq(Document.class));
    }

    @Test
    void getIssuedCertificateReportSummaryStats_allFiltersNull_returnsZerosWithoutNpe() {
        AggregationResults<Document> results = mock(AggregationResults.class);
        when(results.getUniqueMappedResult()).thenReturn(null);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("user_certificates"), eq(Document.class)))
                .thenReturn(results);

        IssuedCertificateReportSummaryDTO summary = repository.getIssuedCertificateReportSummaryStats(
                null, null, null);

        assertEquals(0L, summary.getTotalIssued());
        assertEquals(0L, summary.getThisMonth());
        assertEquals(0L, summary.getThisQuarter());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("user_certificates"), eq(Document.class));
    }
}
