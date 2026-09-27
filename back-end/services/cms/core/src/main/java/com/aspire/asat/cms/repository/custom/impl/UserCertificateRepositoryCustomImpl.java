package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.dto.certificate.CertificateSummaryStatsResponseDTO;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.client.responseDto.ExamCertificateResponseDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.UserCertificate;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.custom.UserCertificateRepositoryCustom;
import com.aspire.asat.cms.util.CertificateStatusUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserCertificateRepositoryCustomImpl implements UserCertificateRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    private final ProductRepository productRepository;

    @Override
    public Page<UserCertificate> findByClientAdminIdWithSearch(
            String clientAdminId, String search, String productId, CertificateStatus status, Pageable pageable) {
        Query query = buildCertificateDetailsQuery(
                Criteria.where("clientAdminId").is(clientAdminId), search, productId, status);
        return executePagedQuery(query, pageable);
    }

    @Override
    public Page<UserCertificate> findByClientAdminIdIn(
            List<String> clientAdminIds, String productId, CertificateStatus status, Pageable pageable) {
        Query query = buildCertificateDetailsQuery(
                Criteria.where("clientAdminId").in(clientAdminIds), null, productId, status);
        return executePagedQuery(query, pageable);
    }

    @Override
    public Page<UserCertificate> findByClientAdminIdInWithSearch(
            List<String> clientAdminIds, String search, String productId, CertificateStatus status, Pageable pageable) {
        Query query = buildCertificateDetailsQuery(
                Criteria.where("clientAdminId").in(clientAdminIds), search, productId, status);
        return executePagedQuery(query, pageable);
    }

    @Override
    public long countByClientAdminIdIn(List<String> clientAdminIds, String productId, CertificateStatus status) {
        Query query = buildCertificateDetailsQuery(
                Criteria.where("clientAdminId").in(clientAdminIds), null, productId, status);
        return mongoTemplate.count(query, UserCertificate.class);
    }

    @Override
    public long countByClientAdminIdInWithSearch(
            List<String> clientAdminIds, String search, String productId, CertificateStatus status) {
        Query query = buildCertificateDetailsQuery(
                Criteria.where("clientAdminId").in(clientAdminIds), search, productId, status);
        return mongoTemplate.count(query, UserCertificate.class);
    }

    @Override
    public Page<UserCertificate> findByUserIdWithSearch(
            String userId, String search, String productId, CertificateStatus status, Pageable pageable) {
        Query query = buildCertificateDetailsQuery(
                Criteria.where("userId").is(userId), search, productId, status);
        return executePagedQuery(query, pageable);
    }

    @Override
    public long countByClientAdminIdWithSearch(
            String clientAdminId, String search, String productId, CertificateStatus status) {
        Query query = buildCertificateDetailsQuery(
                Criteria.where("clientAdminId").is(clientAdminId), search, productId, status);
        return mongoTemplate.count(query, UserCertificate.class);
    }

    @Override
    public long countByUserIdWithSearch(String userId, String search, String productId, CertificateStatus status) {
        Query query = buildCertificateDetailsQuery(
                Criteria.where("userId").is(userId), search, productId, status);
        return mongoTemplate.count(query, UserCertificate.class);
    }

    private Query buildCertificateDetailsQuery(
            Criteria scopeCriteria, String search, String productId, CertificateStatus status) {
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(scopeCriteria);

        if (StringUtils.hasText(productId)) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null && StringUtils.hasText(product.getProductName())) {
                andCriteria.add(Criteria.where("productName").is(product.getProductName()));
            } else {
                andCriteria.add(Criteria.where("_id").is("__no_match__"));
            }
        }

        Criteria statusCriteria = CertificateStatusUtil.buildStatusCriteria(status);
        if (statusCriteria != null) {
            andCriteria.add(statusCriteria);
        }

        if (search != null && !search.isBlank()) {
            String trimmedSearch = search.trim();
            andCriteria.add(new Criteria().orOperator(
                    Criteria.where("productName").regex(trimmedSearch, "i"),
                    Criteria.where("certificateId").regex(trimmedSearch, "i")
            ));
        }

        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        return query;
    }

    private Page<UserCertificate> executePagedQuery(Query query, Pageable pageable) {
        long total = mongoTemplate.count(query, UserCertificate.class);
        query.with(pageable);
        List<UserCertificate> results = mongoTemplate.find(query, UserCertificate.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<ExamCertificateResponseDTO> findExamCertificatesWithJoin(
            String countryId, String mspId, String search, String clientAdminId, String subpackageId, Pageable pageable) {
        log.info("Finding exam certificates with join - countryId: {}, mspId: {}, search: {}, clientAdminId: {}, subpackageId: {}", 
                countryId, mspId, search, clientAdminId, subpackageId);

        // Step 1: Match Exam where examCompleted = true
        Criteria matchCriteria = Criteria.where("examCompleted").is(true);
        if (StringUtils.hasText(countryId)) {
            matchCriteria = matchCriteria.and("countryId").is(countryId);
        }
        if (StringUtils.hasText(mspId)) {
            matchCriteria = matchCriteria.and("mspId").is(mspId);
        }
        if (StringUtils.hasText(clientAdminId)) {
            matchCriteria = matchCriteria.and("clientAdminId").is(clientAdminId);
        }
        if (StringUtils.hasText(subpackageId)) {
            matchCriteria = matchCriteria.and("subPackageId").is(subpackageId);
        }
        MatchOperation matchExam = Aggregation.match(matchCriteria);

        // Step 2: Lookup UserCertificate collection
        // Join on Exam._id (Exams.examId is the @Id -> stored as _id) = UserCertificate.examId
        LookupOperation lookupCertificate = LookupOperation.newLookup()
                .from("user_certificates")
                .localField("_id")
                .foreignField("examId")
                .as("certificate");

        // Step 3: Unwind the certificate array (preserve null/empty arrays to include Exam without certificates)
        UnwindOperation unwindCertificate = Aggregation.unwind("certificate", true);

        // Step 4: Build search criteria for fullName and productName
        // Search can match either UserCertificate fields or Exam fields
        Criteria searchCriteria = null;
        if (StringUtils.hasText(search)) {
            String trimmedSearch = search.trim();
            // Search in certificate fields if certificate exists, or in Exam fields
            searchCriteria = new Criteria().orOperator(
                    Criteria.where("certificate.fullName").regex(trimmedSearch, "i"),
                    Criteria.where("certificate.productName").regex(trimmedSearch, "i"),
                    Criteria.where("title").regex(trimmedSearch, "i")
            );
        }
        // Don't filter out null certificates - we want to include Exam entries even without certificates

        // Step 5: Match after join (only if search criteria exists)
        MatchOperation matchAfterJoin = searchCriteria != null 
                ? Aggregation.match(searchCriteria)
                : null;

        // Step 6: Project required fields with fallbacks from Exam when certificate is null
        ProjectionOperation project = Aggregation.project()
                .and("_id").as("examId") // Exams.examId is the @Id, stored as _id in the exams collection
                .and("examScore").as("examScore")
                .and("examPassed").as("examPassed") // Boolean value - will be converted to "Passed"/"Failed" in mapping
                // Take fullName from Exam
                .and("fullName").as("fullName")
                // Use certificate.expiryDate if exists, otherwise null
                .and("certificate.expiryDate").as("expiryDate")
                // Use certificate.createdAt if exists, otherwise use Exam.examCompletedAt
                .and(ConditionalOperators.ifNull("certificate.createdAt").thenValueOf("examCompletedAt")).as("issueDate")
                // Use certificate.status if exists, otherwise null
                .and("certificate.status").as("status")
                // Use certificate.certificateLink if exists, otherwise use Exam.certificateLink
                .and(ConditionalOperators.ifNull("certificate.certificateLink").thenValueOf("certificateLink")).as("certificateLink")
                // Take productName from Exam
                .and("productName").as("productName")
                // Use certificate.clientAdminId if exists, otherwise use Exam.clientAdminId
                .and(ConditionalOperators.ifNull("certificate.clientAdminId").thenValueOf("clientAdminId")).as("clientAdminId")
                // Use certificate.certificateId if exists, otherwise null
                .and("certificate.certificateId").as("certificateId");

        // Step 7: Build aggregation pipeline
        List<AggregationOperation> operations = new java.util.ArrayList<>();
        operations.add(matchExam);
        operations.add(lookupCertificate);
        operations.add(unwindCertificate);
        if (matchAfterJoin != null) {
            operations.add(matchAfterJoin);
        }
        operations.add(project);
        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));
        
        Aggregation aggregation = Aggregation.newAggregation(operations);

        // Execute aggregation on exams collection
        List<Document> results = mongoTemplate.aggregate(aggregation, "exams", Document.class)
                .getMappedResults();

        // Convert Document to ExamCertificateResponseDTO
        List<ExamCertificateResponseDTO> examCertificates = results.stream()
                .map(this::mapDocumentToExamCertificateDTO)
                .collect(Collectors.toList());

        // Count total records (for pagination)
        long total = countExamCertificatesWithJoin(countryId, mspId, search, clientAdminId, subpackageId);

        log.info("Found {} exam certificates out of {} total", examCertificates.size(), total);
        return new PageImpl<>(examCertificates, pageable, total);
    }

    @Override
    public long countExamCertificatesWithJoin(String countryId, String mspId, String search, String clientAdminId, String subpackageId) {
        log.info("Counting exam certificates with join - countryId: {}, mspId: {}, search: {}, clientAdminId: {}, subpackageId: {}", 
                countryId, mspId, search, clientAdminId, subpackageId);

        // Step 1: Match Exam where examCompleted = true
        Criteria matchCriteria = Criteria.where("examCompleted").is(true);
        if (StringUtils.hasText(countryId)) {
            matchCriteria = matchCriteria.and("countryId").is(countryId);
        }
        if (StringUtils.hasText(mspId)) {
            matchCriteria = matchCriteria.and("mspId").is(mspId);
        }
        if (StringUtils.hasText(clientAdminId)) {
            matchCriteria = matchCriteria.and("clientAdminId").is(clientAdminId);
        }
        if (StringUtils.hasText(subpackageId)) {
            matchCriteria = matchCriteria.and("subPackageId").is(subpackageId);
        }
        MatchOperation matchExam = Aggregation.match(matchCriteria);

        // Step 2: Lookup UserCertificate collection
        // Join on Exam._id (Exams.examId is the @Id -> stored as _id) = UserCertificate.examId
        LookupOperation lookupCertificate = LookupOperation.newLookup()
                .from("user_certificates")
                .localField("_id")
                .foreignField("examId")
                .as("certificate");

        // Step 3: Unwind the certificate array (preserve null/empty arrays)
        UnwindOperation unwindCertificate = Aggregation.unwind("certificate", true);

        // Step 4: Build search criteria (same as find method)
        Criteria searchCriteria = null;
        if (StringUtils.hasText(search)) {
            String trimmedSearch = search.trim();
            searchCriteria = new Criteria().orOperator(
                    Criteria.where("certificate.fullName").regex(trimmedSearch, "i"),
                    Criteria.where("certificate.productName").regex(trimmedSearch, "i"),
                    Criteria.where("title").regex(trimmedSearch, "i")
            );
        }

        // Step 5: Match after join (only if search criteria exists)
        // Don't filter out null certificates - count all Exam entries with examCompleted=true
        MatchOperation matchAfterJoin = searchCriteria != null 
                ? Aggregation.match(searchCriteria)
                : null;

        // Step 6: Count using GroupOperation
        GroupOperation group = Aggregation.group().count().as("total");

        List<AggregationOperation> operations = new java.util.ArrayList<>();
        operations.add(matchExam);
        operations.add(lookupCertificate);
        operations.add(unwindCertificate);
        if (matchAfterJoin != null) {
            operations.add(matchAfterJoin);
        }
        operations.add(group);

        Aggregation aggregation = Aggregation.newAggregation(operations);

        Document result = mongoTemplate.aggregate(aggregation, "exams", Document.class)
                .getUniqueMappedResult();

        long count = result != null ? ((Number) result.get("total")).longValue() : 0L;
        log.info("Total exam certificates count: {}", count);
        return count;
    }

    @Override
    public ExamCertificateResponseDTO findExamCertificateByExamId(String examId) {
        log.info("Finding exam certificate by examId: {}", examId);

        if (!StringUtils.hasText(examId)) {
            log.warn("ExamId is null or empty");
            return null;
        }

        try {
            // Step 1: Match Exam by _id (Exams.examId is the @Id -> stored as _id)
            Criteria matchCriteria = Criteria.where("_id").is(examId);
            MatchOperation matchExam = Aggregation.match(matchCriteria);

            // Step 2: Lookup UserCertificate collection
            // Join on Exam._id = UserCertificate.examId
            LookupOperation lookupCertificate = LookupOperation.newLookup()
                    .from("user_certificates")
                    .localField("_id")
                    .foreignField("examId")
                    .as("certificate");

            // Step 3: Unwind the certificate array (preserve null/empty arrays to include Exam without certificates)
            UnwindOperation unwindCertificate = Aggregation.unwind("certificate", true);

            // Step 4: Project required fields with fallbacks from Exam when certificate is null
            ProjectionOperation project = Aggregation.project()
                    .and("_id").as("examId") // Exams.examId is the @Id, stored as _id in the exams collection
                    .and("examScore").as("examScore")
                    .and("examPassed").as("examPassed")
                    .and("certificate.fullName").as("fullName")
                    .and("certificate.expiryDate").as("expiryDate")
                    .and(ConditionalOperators.ifNull("certificate.createdAt").thenValueOf("examCompletedAt")).as("issueDate")
                    .and("certificate.status").as("status")
                    .and(ConditionalOperators.ifNull("certificate.certificateLink").thenValueOf("certificateLink")).as("certificateLink")
                    .and("certificate.productName").as("productName")
                    .and(ConditionalOperators.ifNull("certificate.clientAdminId").thenValueOf("clientAdminId")).as("clientAdminId")
                    .and("certificate.certificateId").as("certificateId")
                    .andExclude("_id");

            // Step 5: Build aggregation pipeline
            List<AggregationOperation> operations = new java.util.ArrayList<>();
            operations.add(matchExam);
            operations.add(lookupCertificate);
            operations.add(unwindCertificate);
            operations.add(project);

            Aggregation aggregation = Aggregation.newAggregation(operations);

            // Execute aggregation on exams collection
            Document result = mongoTemplate.aggregate(aggregation, "exams", Document.class)
                    .getUniqueMappedResult();

            if (result == null) {
                log.warn("No exam certificate found for examId: {}", examId);
                return null;
            }

            // Convert Document to ExamCertificateResponseDTO
            ExamCertificateResponseDTO examCertificate = mapDocumentToExamCertificateDTO(result);
            log.info("Successfully found exam certificate for examId: {}", examId);
            return examCertificate;

        } catch (Exception e) {
            log.error("Error finding exam certificate by examId: {}", examId, e);
            return null;
        }
    }

    /**
     * Maps a MongoDB Document to ExamCertificateResponseDTO.
     */
    private ExamCertificateResponseDTO mapDocumentToExamCertificateDTO(Document doc) {
        return ExamCertificateResponseDTO.builder()
                .examId(getStringValue(doc, "examId"))
                .examScore(getDoubleValue(doc, "examScore"))
                .examPassed(convertExamPassedToString(doc, "examPassed"))
                .fullName(getStringValue(doc, "fullName"))
                .expiryDate(getInstantValue(doc, "expiryDate"))
                .issueDate(getInstantValue(doc, "issueDate"))
                .status(resolveStatus(doc))
                .certificateLink(getStringValue(doc, "certificateLink"))
                .productName(getStringValue(doc, "productName"))
                .clientAdminId(getStringValue(doc, "clientAdminId"))
                .certificateId(getStringValue(doc, "certificateId"))
                .build();
    }

    /**
     * Resolves the certificate status: when the exam was not passed the status is "INVALID",
     * otherwise the certificate's own status value is used.
     */
    private String resolveStatus(Document doc) {
        Boolean passed = getBooleanValue(doc, "examPassed");
        if (passed == null || !passed) {
            return "INVALID";
        }
        return getStringValue(doc, "status");
    }

    /**
     * Converts boolean examPassed value to "Passed" or "Failed" string.
     *
     * @param doc the MongoDB document
     * @param key the field key
     * @return "Passed" if true, "Failed" if false, null if null
     */
    private String convertExamPassedToString(Document doc, String key) {
        Object value = doc.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "Passed" : "Failed";
        }
        // Handle string values if they exist
        if (value instanceof String) {
            String strValue = ((String) value).trim().toLowerCase();
            if ("true".equals(strValue) || "passed".equals(strValue)) {
                return "Passed";
            } else if ("false".equals(strValue) || "failed".equals(strValue)) {
                return "Failed";
            }
        }
        return null;
    }

    private Double getDoubleValue(Document doc, String key) {
        Object value = doc.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private Boolean getBooleanValue(Document doc, String key) {
        Object value = doc.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

    private String getStringValue(Document doc, String key) {
        Object value = doc.get(key);
        return value != null ? value.toString() : null;
    }

    private Instant getInstantValue(Document doc, String key) {
        Object value = doc.get(key);
        if (value == null) return null;
        if (value instanceof Instant) {
            return (Instant) value;
        }
        if (value instanceof org.bson.BsonDateTime) {
            return Instant.ofEpochMilli(((org.bson.BsonDateTime) value).getValue());
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue());
        }
        return null;
    }

    @Override
    public CertificateSummaryStatsResponseDTO getCertificateSummaryStats(
            String productId, Instant fromDate, Instant toDate) {
        return buildCertificateSummaryStats(productId, fromDate, toDate, null);
    }

    @Override
    public CertificateSummaryStatsResponseDTO getCertificateSummaryStatsForClientAdmins(
            List<String> clientAdminIds, String productId, Instant fromDate, Instant toDate) {
        return buildCertificateSummaryStats(productId, fromDate, toDate, clientAdminIds);
    }

    private CertificateSummaryStatsResponseDTO buildCertificateSummaryStats(
            String productId, Instant fromDate, Instant toDate, List<String> clientAdminIds) {
        long totalCertificatesIssued = getTotalCertificatesIssued(productId, fromDate, toDate, clientAdminIds);
        long activeCertificatesCount = getActiveCertificatesCount(productId, fromDate, toDate, clientAdminIds);
        double averageCompletionRate = getAverageCompletionRate(productId, fromDate, toDate, clientAdminIds);
        long expiringNextThirtyDays = getExpiringNextThirtyDaysCount(productId, clientAdminIds);

        return CertificateSummaryStatsResponseDTO.builder()
                .totalCertificatesIssued(totalCertificatesIssued)
                .activeCertificatesCount(activeCertificatesCount)
                .averageCompletionRate(averageCompletionRate)
                .expiringThisMonth(expiringNextThirtyDays)
                .build();
    }

    @Override
    public ExpiredCertificateReportSummaryDTO getCertificateReportSummaryStats(
            String clientAdminId, Instant fromDate, Instant toDate, Integer thresholdDays, String productId) {
        return buildCertificateReportSummaryStats(clientAdminId, null, fromDate, toDate, thresholdDays, productId);
    }

    @Override
    public ExpiredCertificateReportSummaryDTO getCertificateReportSummaryStatsForClientAdminIds(
            List<String> clientAdminIds, Instant fromDate, Instant toDate, Integer thresholdDays, String productId) {
        return buildCertificateReportSummaryStats(null, clientAdminIds, fromDate, toDate, thresholdDays, productId);
    }

    private ExpiredCertificateReportSummaryDTO buildCertificateReportSummaryStats(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant fromDate,
            Instant toDate,
            Integer thresholdDays,
            String productId) {
        int effectiveThreshold = (thresholdDays != null && thresholdDays > 0) ? thresholdDays : 60;
        Instant now = Instant.now();
        Instant thresholdDate = now.plus(effectiveThreshold, ChronoUnit.DAYS);
        Query baseQuery = buildReportQuery(clientAdminId, clientAdminIds, productId, fromDate, toDate, null, null, null, "expiryDate");

        List<AggregationOperation> operations = new java.util.ArrayList<>();
        Document queryObject = baseQuery.getQueryObject();
        if (!queryObject.isEmpty()) {
            operations.add(context -> new Document("$match", queryObject));
        }

        Date nowDate = Date.from(now);
        Date thresholdDateAsDate = Date.from(thresholdDate);

        operations.add(context -> new Document("$project", new Document()
                .append("isExpired", new Document("$cond", List.of(
                        new Document("$or", List.of(
                                new Document("$eq", Arrays.asList("$expiryDate", null)),
                                new Document("$lt", List.of("$expiryDate", nowDate))
                        )),
                        1, 0
                )))
                .append("isExpiringSoon", new Document("$cond", List.of(
                        new Document("$and", List.of(
                                new Document("$ne", Arrays.asList("$expiryDate", null)),
                                new Document("$gte", List.of("$expiryDate", nowDate)),
                                new Document("$lte", List.of("$expiryDate", thresholdDateAsDate))
                        )),
                        1, 0
                )))
                .append("isValid", new Document("$cond", List.of(
                        new Document("$gt", List.of("$expiryDate", thresholdDateAsDate)),
                        1, 0
                )))
        ));

        operations.add(context -> new Document("$group", new Document("_id", null)
                .append("totalCertificates", new Document("$sum", 1))
                .append("totalValidCertificates", new Document("$sum", "$isValid"))
                .append("totalExpiredCertificates", new Document("$sum", "$isExpired"))
                .append("totalExpiringCertificates", new Document("$sum", "$isExpiringSoon"))
        ));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        Document summary = mongoTemplate.aggregate(aggregation, "user_certificates", Document.class).getUniqueMappedResult();
        if (summary == null) {
            return ExpiredCertificateReportSummaryDTO.builder()
                    .totalCertificates(0L)
                    .totalValidCertificates(0L)
                    .totalExpiredCertificates(0L)
                    .totalExpiringCertificates(0L)
                    .build();
        }

        return ExpiredCertificateReportSummaryDTO.builder()
                .totalCertificates(((Number) summary.getOrDefault("totalCertificates", 0)).longValue())
                .totalValidCertificates(((Number) summary.getOrDefault("totalValidCertificates", 0)).longValue())
                .totalExpiredCertificates(((Number) summary.getOrDefault("totalExpiredCertificates", 0)).longValue())
                .totalExpiringCertificates(((Number) summary.getOrDefault("totalExpiringCertificates", 0)).longValue())
                .build();
    }

    @Override
    public Page<UserCertificate> findCertificatesForReport(
            String clientAdminId,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField,
            Pageable pageable) {
        Query query = buildReportQuery(clientAdminId, null, productId, fromDate, toDate, search, status, thresholdDays, dateField);
        query.with(pageable);
        List<UserCertificate> records = mongoTemplate.find(query, UserCertificate.class);
        return PageableExecutionUtils.getPage(records, pageable, () -> {
            Query countQuery = buildReportQuery(clientAdminId, null, productId, fromDate, toDate, search, status, thresholdDays, dateField);
            return mongoTemplate.count(countQuery, UserCertificate.class);
        });
    }

    @Override
    public Page<UserCertificate> findCertificatesForReportByClientAdminIds(
            List<String> clientAdminIds,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField,
            Pageable pageable) {
        Query query = buildReportQuery(null, clientAdminIds, productId, fromDate, toDate, search, status, thresholdDays, dateField);
        query.with(pageable);
        List<UserCertificate> records = mongoTemplate.find(query, UserCertificate.class);
        return PageableExecutionUtils.getPage(records, pageable, () -> {
            Query countQuery = buildReportQuery(null, clientAdminIds, productId, fromDate, toDate, search, status, thresholdDays, dateField);
            return mongoTemplate.count(countQuery, UserCertificate.class);
        });
    }

    @Override
    public List<UserCertificate> findCertificatesForReportExport(
            String clientAdminId,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField) {
        Query query = buildReportQuery(clientAdminId, null, productId, fromDate, toDate, search, status, thresholdDays, dateField);
        return mongoTemplate.find(query, UserCertificate.class);
    }

    @Override
    public List<UserCertificate> findCertificatesForReportExportByClientAdminIds(
            List<String> clientAdminIds,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField) {
        Query query = buildReportQuery(null, clientAdminIds, productId, fromDate, toDate, search, status, thresholdDays, dateField);
        return mongoTemplate.find(query, UserCertificate.class);
    }

    @Override
    public IssuedCertificateReportSummaryDTO getIssuedCertificateReportSummaryStats(
            String clientAdminId, Instant fromDate, Instant toDate) {
        return buildIssuedCertificateReportSummaryStats(clientAdminId, null, fromDate, toDate);
    }

    @Override
    public IssuedCertificateReportSummaryDTO getIssuedCertificateReportSummaryStatsForClientAdminIds(
            List<String> clientAdminIds, Instant fromDate, Instant toDate) {
        return buildIssuedCertificateReportSummaryStats(null, clientAdminIds, fromDate, toDate);
    }

    private IssuedCertificateReportSummaryDTO buildIssuedCertificateReportSummaryStats(
            String clientAdminId,
            List<String> clientAdminIds,
            Instant fromDate,
            Instant toDate) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        Instant monthStart = today.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        int quarterMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
        Instant quarterStart = today.withMonth(quarterMonth).withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Query baseQuery = buildReportQuery(clientAdminId, clientAdminIds, null, fromDate, toDate, null, null, null, "createdAt");
        List<AggregationOperation> operations = new ArrayList<>();
        Document queryObject = baseQuery.getQueryObject();
        if (!queryObject.isEmpty()) {
            operations.add(context -> new Document("$match", queryObject));
        }

        Date monthStartDate = Date.from(monthStart);
        Date quarterStartDate = Date.from(quarterStart);
        Date nowDate = Date.from(now);

        operations.add(context -> new Document("$project", new Document()
                .append("isThisMonth", new Document("$cond", List.of(
                        new Document("$and", List.of(
                                new Document("$ne", Arrays.asList("$createdAt", null)),
                                new Document("$gte", List.of("$createdAt", monthStartDate)),
                                new Document("$lte", List.of("$createdAt", nowDate))
                        )),
                        1, 0
                )))
                .append("isThisQuarter", new Document("$cond", List.of(
                        new Document("$and", List.of(
                                new Document("$ne", Arrays.asList("$createdAt", null)),
                                new Document("$gte", List.of("$createdAt", quarterStartDate)),
                                new Document("$lte", List.of("$createdAt", nowDate))
                        )),
                        1, 0
                )))
        ));

        operations.add(context -> new Document("$group", new Document("_id", null)
                .append("totalIssued", new Document("$sum", 1))
                .append("thisMonth", new Document("$sum", "$isThisMonth"))
                .append("thisQuarter", new Document("$sum", "$isThisQuarter"))
        ));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        Document summary = mongoTemplate.aggregate(aggregation, "user_certificates", Document.class).getUniqueMappedResult();
        if (summary == null) {
            return IssuedCertificateReportSummaryDTO.builder()
                    .totalIssued(0L)
                    .thisMonth(0L)
                    .thisQuarter(0L)
                    .build();
        }

        return IssuedCertificateReportSummaryDTO.builder()
                .totalIssued(((Number) summary.getOrDefault("totalIssued", 0)).longValue())
                .thisMonth(((Number) summary.getOrDefault("thisMonth", 0)).longValue())
                .thisQuarter(((Number) summary.getOrDefault("thisQuarter", 0)).longValue())
                .build();
    }

    private Query buildReportQuery(
            String clientAdminId,
            List<String> clientAdminIds,
            String productId,
            Instant fromDate,
            Instant toDate,
            String search,
            CertificateStatus status,
            Integer thresholdDays,
            String dateField) {
        Query query = new Query();
        java.util.List<Criteria> andCriteria = new java.util.ArrayList<>();

        if (StringUtils.hasText(clientAdminId)) {
            andCriteria.add(Criteria.where("clientAdminId").is(clientAdminId));
        } else if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            andCriteria.add(Criteria.where("clientAdminId").in(clientAdminIds));
        }

        if (StringUtils.hasText(productId)) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null && StringUtils.hasText(product.getProductName())) {
                andCriteria.add(Criteria.where("productName").is(product.getProductName()));
            } else {
                andCriteria.add(Criteria.where("_id").is("__no_match__"));
            }
        }

        String effectiveDateField = StringUtils.hasText(dateField) ? dateField : "expiryDate";
        if (fromDate != null || toDate != null) {
            Criteria dateCriteria = Criteria.where(effectiveDateField);
            if (fromDate != null) {
                dateCriteria = dateCriteria.gte(fromDate);
            }
            if (toDate != null) {
                dateCriteria = dateCriteria.lte(toDate);
            }
            andCriteria.add(dateCriteria);
        }

        int effectiveThreshold = (thresholdDays != null && thresholdDays > 0)
                ? thresholdDays
                : CertificateStatusUtil.EXPIRING_SOON_DAYS;
        if (status != null) {
            Criteria statusCriteria = CertificateStatusUtil.buildStatusCriteria(status, effectiveThreshold);
            if (statusCriteria != null) {
                andCriteria.add(statusCriteria);
            }
        } else if ("expiryDate".equals(effectiveDateField)) {
            andCriteria.add(CertificateStatusUtil.buildExpiredOrExpiringSoonCriteria(effectiveThreshold));
        }

        if (StringUtils.hasText(search)) {
            String trimmedSearch = search.trim();
            andCriteria.add(new Criteria().orOperator(
                    Criteria.where("certificateId").regex(trimmedSearch, "i"),
                    Criteria.where("fullName").regex(trimmedSearch, "i"),
                    Criteria.where("productName").regex(trimmedSearch, "i")
            ));
        }

        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        }
        return query;
    }

    private long getTotalCertificatesIssued(
            String productId, Instant fromDate, Instant toDate, List<String> clientAdminIds) {
        Query query = new Query();
        Criteria criteria = null;

        if (StringUtils.hasText(productId)) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null && StringUtils.hasText(product.getProductName())) {
                criteria = Criteria.where("productName").is(product.getProductName());
            } else {
                return 0L;
            }
        }

        if (fromDate != null && toDate != null) {
            criteria = criteria == null
                    ? Criteria.where("createdAt").gte(fromDate).lte(toDate)
                    : criteria.and("createdAt").gte(fromDate).lte(toDate);
        } else if (fromDate != null) {
            criteria = criteria == null ? Criteria.where("createdAt").gte(fromDate) : criteria.and("createdAt").gte(fromDate);
        } else if (toDate != null) {
            criteria = criteria == null ? Criteria.where("createdAt").lte(toDate) : criteria.and("createdAt").lte(toDate);
        }

        criteria = applyClientAdminIdsCriteria(criteria, clientAdminIds);
        if (criteria != null) {
            query.addCriteria(criteria);
        }
        return mongoTemplate.count(query, UserCertificate.class);
    }

    private long getActiveCertificatesCount(
            String productId, Instant fromDate, Instant toDate, List<String> clientAdminIds) {
        Instant currentDate = Instant.now();
        Criteria criteria = Criteria.where("expiryDate").gt(currentDate);

        if (StringUtils.hasText(productId)) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null && StringUtils.hasText(product.getProductName())) {
                criteria = criteria.and("productName").is(product.getProductName());
            } else {
                return 0L;
            }
        }

        if (fromDate != null && toDate != null) {
            criteria = criteria.and("createdAt").gte(fromDate).lte(toDate);
        } else if (fromDate != null) {
            criteria = criteria.and("createdAt").gte(fromDate);
        } else if (toDate != null) {
            criteria = criteria.and("createdAt").lte(toDate);
        }

        criteria = applyClientAdminIdsCriteria(criteria, clientAdminIds);
        return mongoTemplate.count(new Query(criteria), UserCertificate.class);
    }

    private double getAverageCompletionRate(
            String productId, Instant fromDate, Instant toDate, List<String> clientAdminIds) {
        LocalDate fromLocalDate = fromDate != null ? fromDate.atZone(ZoneId.systemDefault()).toLocalDate() : null;
        LocalDate toLocalDate = toDate != null ? toDate.atZone(ZoneId.systemDefault()).toLocalDate() : null;

        Criteria totalCriteria = new Criteria();
        if (StringUtils.hasText(productId)) {
            totalCriteria = totalCriteria.and("productId").is(productId);
        }
        if (fromLocalDate != null && toLocalDate != null) {
            totalCriteria = totalCriteria.and("assignedDate").gte(fromLocalDate).lte(toLocalDate);
        } else if (fromLocalDate != null) {
            totalCriteria = totalCriteria.and("assignedDate").gte(fromLocalDate);
        } else if (toLocalDate != null) {
            totalCriteria = totalCriteria.and("assignedDate").lte(toLocalDate);
        }
        totalCriteria = applyClientAdminIdsCriteria(totalCriteria, clientAdminIds);

        long totalAssigned = mongoTemplate.count(new Query(totalCriteria), UserSubPackage.class);
        if (totalAssigned == 0) {
            return 0.0;
        }

        Criteria completedCriteria = totalCriteria.and("examCompleted").is(true);
        long totalCompleted = mongoTemplate.count(new Query(completedCriteria), UserSubPackage.class);
        return (totalCompleted * 100.0) / totalAssigned;
    }

    private long getExpiringNextThirtyDaysCount(String productId, List<String> clientAdminIds) {
        Instant now = Instant.now();
        Instant expiryThreshold = now.plus(30, ChronoUnit.DAYS);
        Criteria criteria = Criteria.where("expiryDate").gte(now).lte(expiryThreshold);

        if (StringUtils.hasText(productId)) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null && StringUtils.hasText(product.getProductName())) {
                criteria = criteria.and("productName").is(product.getProductName());
            } else {
                return 0L;
            }
        }

        criteria = applyClientAdminIdsCriteria(criteria, clientAdminIds);
        return mongoTemplate.count(new Query(criteria), UserCertificate.class);
    }

    private Criteria applyClientAdminIdsCriteria(Criteria criteria, List<String> clientAdminIds) {
        if (clientAdminIds == null || clientAdminIds.isEmpty()) {
            return criteria;
        }
        return criteria == null
                ? Criteria.where("clientAdminId").in(clientAdminIds)
                : criteria.and("clientAdminId").in(clientAdminIds);
    }
}

