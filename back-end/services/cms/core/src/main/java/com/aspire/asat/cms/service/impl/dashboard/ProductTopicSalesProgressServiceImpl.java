package com.aspire.asat.cms.service.impl.dashboard;

import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.dashboard.ProductTopicSalesProgressResponseDto;
import com.aspire.asat.cms.dto.enums.TimeFrame;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.exception.CmsServiceException;
import com.aspire.asat.cms.model.ClientProductReplica;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ClientProductReplicaRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.service.dashboard.ProductTopicSalesProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service implementation for product-topic sales progress analytics
 * Refactored to use ClientProductReplica for products, then count topics per product per month
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductTopicSalesProgressServiceImpl implements ProductTopicSalesProgressService {

    private final MongoTemplate mongoTemplate;
    private final ClientProductReplicaRepository clientProductReplicaRepository;
    private final ProductRepository productRepository;
    private final ClientAdminServiceClient clientAdminServiceClient;

    // Month names for response
    private static final String[] MONTH_NAMES = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    @Override
    public ProductTopicSalesProgressResponseDto getProductTopicSalesProgress(TimeFrame timeFrame) {
        if (timeFrame == null) {
            timeFrame = TimeFrame.MONTHLY;
        }

        log.info("Calculating product-topic sales progress with timeFrame: {}", timeFrame);

        try {
            Set<String> productIds = getUniqueProductsFromClientProductReplica();
            log.info("Found {} unique products", productIds.size());
            return buildSalesProgress(timeFrame, productIds);
        } catch (Exception e) {
            log.error("Error in getProductTopicSalesProgress: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ProductTopicSalesProgressResponseDto getMspProductTopicSalesProgress(TimeFrame timeFrame, String mspId) {
        if (timeFrame == null) {
            timeFrame = TimeFrame.MONTHLY;
        }

        if (mspId == null || mspId.isBlank()) {
            throw new CmsServiceException("mspId is required", HttpStatus.BAD_REQUEST);
        }

        log.info("Calculating MSP product-topic sales progress for mspId={} timeFrame={}", mspId, timeFrame);

        try {
            List<String> clientAdminIds = clientAdminServiceClient.getClientAdminIdsByMspId(mspId.trim());
            if (clientAdminIds == null || clientAdminIds.isEmpty()) {
                log.info("No client admins found for mspId={}", mspId);
                return buildEmptyResponse(timeFrame);
            }

            Set<String> uniqueClientAdminIds = clientAdminIds.stream()
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Set<String> productIds = getUniqueProductsForClientAdmins(new ArrayList<>(uniqueClientAdminIds));
            log.info("Found {} unique products for mspId={} across {} clients",
                    productIds.size(), mspId, uniqueClientAdminIds.size());

            return buildSalesProgress(timeFrame, productIds);
        } catch (CmsServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getMspProductTopicSalesProgress for mspId={}: {}", mspId, e.getMessage(), e);
            throw e;
        }
    }

    private ProductTopicSalesProgressResponseDto buildSalesProgress(TimeFrame timeFrame, Set<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return buildEmptyResponse(timeFrame);
        }

        Map<String, String> productIdToNameMap = getProductNames(productIds);
        log.info("Retrieved product names for {} products", productIdToNameMap.size());

        if (timeFrame == TimeFrame.MONTHLY) {
            LocalDate now = LocalDate.now(ZoneId.of("UTC"));
            int currentYear = now.getYear();
            Instant yearStart = LocalDate.of(currentYear, 1, 1)
                    .atStartOfDay(ZoneId.of("UTC")).toInstant();
            Instant yearEnd = LocalDate.of(currentYear, 12, 31)
                    .atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant();

            Map<String, Map<Integer, Long>> productMonthCounts = calculateTopicCountsByProductAndMonth(
                    productIds, yearStart, yearEnd);
            return buildMonthlyResponse(productIdToNameMap, productMonthCounts);
        }

        LocalDate now = LocalDate.now(ZoneId.of("UTC"));
        int currentYear = now.getYear();
        int startYear = currentYear - 5;
        Instant yearStart = LocalDate.of(startYear, 1, 1)
                .atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant yearEnd = LocalDate.of(currentYear, 12, 31)
                .atTime(23, 59, 59).atZone(ZoneId.of("UTC")).toInstant();

        Map<String, Map<Integer, Long>> productYearCounts = calculateTopicCountsByProductAndYear(
                productIds, yearStart, yearEnd, startYear, currentYear);
        return buildYearlyResponse(productIdToNameMap, productYearCounts, startYear, currentYear);
    }

    /**
     * Get unique product IDs from ClientProductReplica model
     */
    private Set<String> getUniqueProductsFromClientProductReplica() {
        List<Document> pipeline = new ArrayList<>();

        pipeline.add(new Document("$group",
            new Document("_id", "$productId")));

        pipeline.add(new Document("$project",
            new Document("productId", "$_id")));

        String collectionName = mongoTemplate.getCollectionName(ClientProductReplica.class);
        List<Document> results = mongoTemplate.getCollection(collectionName)
                .aggregate(pipeline)
                .into(new ArrayList<>());

        return results.stream()
                .map(doc -> doc.getString("productId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Get unique product IDs from ClientProductReplica for the given client admins
     */
    private Set<String> getUniqueProductsForClientAdmins(List<String> clientAdminIds) {
        if (clientAdminIds == null || clientAdminIds.isEmpty()) {
            return Collections.emptySet();
        }

        return clientProductReplicaRepository.findByClientAdminIdIn(clientAdminIds).stream()
                .map(ClientProductReplica::getProductId)
                .filter(productId -> productId != null && !productId.isBlank())
                .collect(Collectors.toSet());
    }

    /**
     * Get product names by product IDs
     */
    private Map<String, String> getProductNames(Set<String> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        return products.stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        Product::getProductName,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Calculate topic counts per product per month using simple MongoDB aggregation
     * For each product, count active (ENABLED) topics created in each month of current year
     */
    private Map<String, Map<Integer, Long>> calculateTopicCountsByProductAndMonth(
            Set<String> productIds, Instant yearStart, Instant yearEnd) {

        log.debug("Calculating topic counts for {} products from {} to {}",
                productIds.size(), yearStart, yearEnd);

        List<Document> pipeline = new ArrayList<>();

        pipeline.add(new Document("$match",
            new Document("status", TopicStatus.ENABLED.name())
                .append("createdAt", new Document("$gte", yearStart).append("$lte", yearEnd))
                .append("productPackageMappings", new Document("$exists", true)
                    .append("$ne", Collections.emptyList()))
        ));

        pipeline.add(new Document("$unwind", "$productPackageMappings"));

        pipeline.add(new Document("$addFields",
            new Document("month", new Document("$month", "$createdAt"))
        ));

        pipeline.add(new Document("$project",
            new Document("productId", "$productPackageMappings.productId")
                .append("month", 1)
                .append("topicId", "$_id")
        ));

        pipeline.add(new Document("$match",
            new Document("productId", new Document("$in", new ArrayList<>(productIds)))
        ));

        pipeline.add(new Document("$group",
            new Document("_id",
                new Document("productId", "$productId")
                    .append("month", "$month"))
                .append("uniqueTopics", new Document("$addToSet", "$topicId"))
        ));

        pipeline.add(new Document("$project",
            new Document("productId", "$_id.productId")
                .append("month", "$_id.month")
                .append("count", new Document("$size", "$uniqueTopics"))
        ));

        String collectionName = mongoTemplate.getCollectionName(Topic.class);
        log.debug("Executing aggregation pipeline on collection: {}", collectionName);

        List<Document> results = mongoTemplate.getCollection(collectionName)
                .aggregate(pipeline)
                .into(new ArrayList<>());

        log.debug("Aggregation returned {} results", results.size());

        Map<String, Map<Integer, Long>> productMonthCounts = new HashMap<>();

        for (Document doc : results) {
            String productId = doc.getString("productId");
            Integer month = doc.getInteger("month");
            Number countNumber = (Number) doc.get("count");
            Long count = countNumber != null ? countNumber.longValue() : 0L;

            if (productId != null && month != null && month >= 1 && month <= 12) {
                productMonthCounts.computeIfAbsent(productId, k -> new HashMap<>())
                        .put(month, count);
            }
        }

        log.debug("Calculated topic counts for {} products across months", productMonthCounts.size());
        return productMonthCounts;
    }

    /**
     * Calculate topic counts per product per year (for previous 6 years including current year)
     */
    private Map<String, Map<Integer, Long>> calculateTopicCountsByProductAndYear(
            Set<String> productIds, Instant yearStart, Instant yearEnd, int startYear, int endYear) {

        log.debug("Calculating topic counts for {} products from year {} to {}",
                productIds.size(), startYear, endYear);

        List<Document> pipeline = new ArrayList<>();

        pipeline.add(new Document("$match",
            new Document("status", TopicStatus.ENABLED.name())
                .append("createdAt", new Document("$gte", yearStart).append("$lte", yearEnd))
                .append("productPackageMappings", new Document("$exists", true)
                    .append("$ne", Collections.emptyList()))
        ));

        pipeline.add(new Document("$unwind", "$productPackageMappings"));

        pipeline.add(new Document("$addFields",
            new Document("year", new Document("$year", "$createdAt"))
        ));

        pipeline.add(new Document("$project",
            new Document("productId", "$productPackageMappings.productId")
                .append("year", 1)
                .append("topicId", "$_id")
        ));

        pipeline.add(new Document("$match",
            new Document("productId", new Document("$in", new ArrayList<>(productIds)))
        ));

        pipeline.add(new Document("$group",
            new Document("_id",
                new Document("productId", "$productId")
                    .append("year", "$year"))
                .append("uniqueTopics", new Document("$addToSet", "$topicId"))
        ));

        pipeline.add(new Document("$project",
            new Document("productId", "$_id.productId")
                .append("year", "$_id.year")
                .append("count", new Document("$size", "$uniqueTopics"))
        ));

        String collectionName = mongoTemplate.getCollectionName(Topic.class);
        log.debug("Executing yearly aggregation pipeline on collection: {}", collectionName);

        List<Document> results = mongoTemplate.getCollection(collectionName)
                .aggregate(pipeline)
                .into(new ArrayList<>());

        log.debug("Yearly aggregation returned {} results", results.size());

        Map<String, Map<Integer, Long>> productYearCounts = new HashMap<>();

        for (Document doc : results) {
            String productId = doc.getString("productId");
            Integer year = doc.getInteger("year");
            Number countNumber = (Number) doc.get("count");
            Long count = countNumber != null ? countNumber.longValue() : 0L;

            if (productId != null && year != null && year >= startYear && year <= endYear) {
                productYearCounts.computeIfAbsent(productId, k -> new HashMap<>())
                        .put(year, count);
            }
        }

        log.debug("Calculated topic counts for {} products across years", productYearCounts.size());
        return productYearCounts;
    }

    private ProductTopicSalesProgressResponseDto buildMonthlyResponse(
            Map<String, String> productIdToNameMap,
            Map<String, Map<Integer, Long>> productMonthCounts) {

        List<String> productCategories = productIdToNameMap.values().stream()
                .sorted()
                .collect(Collectors.toList());

        List<Map<String, Object>> seriesData = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    Map<String, Object> monthData = new LinkedHashMap<>();
                    monthData.put("month", MONTH_NAMES[month - 1]);

                    for (Map.Entry<String, String> entry : productIdToNameMap.entrySet()) {
                        String productId = entry.getKey();
                        String productName = entry.getValue();

                        Map<Integer, Long> monthCounts = productMonthCounts.get(productId);
                        Long count = (monthCounts != null && monthCounts.containsKey(month))
                                ? monthCounts.get(month)
                                : 0L;

                        monthData.put(productName, count);
                    }

                    return monthData;
                })
                .collect(Collectors.toList());

        return ProductTopicSalesProgressResponseDto.builder()
                .chartTitle("Sales Progress by Products")
                .timeFrame(TimeFrame.MONTHLY.getValue())
                .productCategories(productCategories)
                .seriesData(seriesData)
                .build();
    }

    private ProductTopicSalesProgressResponseDto buildYearlyResponse(
            Map<String, String> productIdToNameMap,
            Map<String, Map<Integer, Long>> productYearCounts,
            int startYear, int endYear) {

        List<String> productCategories = productIdToNameMap.values().stream()
                .sorted()
                .collect(Collectors.toList());

        List<Map<String, Object>> seriesData = IntStream.rangeClosed(startYear, endYear)
                .mapToObj(year -> {
                    Map<String, Object> yearData = new LinkedHashMap<>();
                    yearData.put("year", String.valueOf(year));

                    for (Map.Entry<String, String> entry : productIdToNameMap.entrySet()) {
                        String productId = entry.getKey();
                        String productName = entry.getValue();

                        Map<Integer, Long> yearCounts = productYearCounts.get(productId);
                        Long count = (yearCounts != null && yearCounts.containsKey(year))
                                ? yearCounts.get(year)
                                : 0L;

                        yearData.put(productName, count);
                    }

                    return yearData;
                })
                .collect(Collectors.toList());

        return ProductTopicSalesProgressResponseDto.builder()
                .chartTitle("Sales Progress by Products")
                .timeFrame(TimeFrame.YEARLY.getValue())
                .productCategories(productCategories)
                .seriesData(seriesData)
                .build();
    }

    private ProductTopicSalesProgressResponseDto buildEmptyResponse(TimeFrame timeFrame) {
        String timeFrameValue = (timeFrame != null) ? timeFrame.getValue() : TimeFrame.MONTHLY.getValue();
        return ProductTopicSalesProgressResponseDto.builder()
                .chartTitle("Sales Progress by Products")
                .timeFrame(timeFrameValue)
                .productCategories(Collections.emptyList())
                .seriesData(Collections.emptyList())
                .build();
    }
}
