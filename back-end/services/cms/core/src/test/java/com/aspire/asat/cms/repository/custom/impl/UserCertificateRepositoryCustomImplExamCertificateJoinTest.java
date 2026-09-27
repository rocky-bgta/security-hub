package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.client.responseDto.ExamCertificateResponseDTO;
import com.aspire.asat.cms.repository.ProductRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCertificateRepositoryCustomImplExamCertificateJoinTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UserCertificateRepositoryCustomImpl repository;

    @Test
    void findExamCertificatesWithJoin_mapsExamFullNameAndProductName() {
        Document passedExam = new Document()
                .append("examId", "exam-1")
                .append("examScore", 80.0)
                .append("examPassed", true)
                .append("fullName", "John Doe")
                .append("productName", "Security Awareness Training")
                .append("status", "VALID")
                .append("clientAdminId", "client-admin-1");

        AggregationResults<Document> results = mock(AggregationResults.class);
        AggregationResults<Document> countResults = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(List.of(passedExam));
        when(countResults.getUniqueMappedResult()).thenReturn(new Document("total", 1));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("exams"), eq(Document.class)))
                .thenReturn(results, countResults);

        Page<ExamCertificateResponseDTO> page = repository.findExamCertificatesWithJoin(
                null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        ExamCertificateResponseDTO dto = page.getContent().get(0);
        assertEquals("exam-1", dto.getExamId());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("Security Awareness Training", dto.getProductName());
        assertEquals("VALID", dto.getStatus());
        assertEquals("Passed", dto.getExamPassed());
    }

    @Test
    void findExamCertificatesWithJoin_setsStatusInvalidWhenExamFailed() {
        Document failedExam = new Document()
                .append("examId", "exam-2")
                .append("examScore", 40.0)
                .append("examPassed", false)
                .append("fullName", "Jane Smith")
                .append("productName", "Phishing Basics")
                .append("status", "VALID")
                .append("clientAdminId", "client-admin-1");

        AggregationResults<Document> results = mock(AggregationResults.class);
        AggregationResults<Document> countResults = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(List.of(failedExam));
        when(countResults.getUniqueMappedResult()).thenReturn(new Document("total", 1));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("exams"), eq(Document.class)))
                .thenReturn(results, countResults);

        Page<ExamCertificateResponseDTO> page = repository.findExamCertificatesWithJoin(
                null, null, null, null, null, PageRequest.of(0, 10));

        ExamCertificateResponseDTO dto = page.getContent().get(0);
        assertEquals("INVALID", dto.getStatus());
        assertEquals("Failed", dto.getExamPassed());
        assertEquals("Jane Smith", dto.getFullName());
        assertEquals("Phishing Basics", dto.getProductName());
    }
}
