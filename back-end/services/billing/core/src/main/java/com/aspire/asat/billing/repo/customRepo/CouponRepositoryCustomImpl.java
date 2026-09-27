package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.model.Coupon;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@Repository
public class CouponRepositoryCustomImpl implements CouponRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public CouponRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<Coupon> incrementUsageIfBelowLimit(String couponId) {
        Document filter = new Document("_id", couponId)
                .append("$expr", new Document("$lt", Arrays.asList(
                        new Document("$ifNull", Arrays.asList("$totalUsed", 0)),
                        "$usageLimit"
                )));

        Update update = new Update()
                .inc("totalUsed", 1)
                .set("updatedAt", Instant.now());

        Coupon updated = mongoTemplate.findAndModify(
                new BasicQuery(filter),
                update,
                FindAndModifyOptions.options().returnNew(true),
                Coupon.class
        );

        return Optional.ofNullable(updated);
    }

    @Override
    public Optional<Coupon> decrementUsageIfAboveZero(String couponId) {
        Document filter = new Document("_id", couponId)
                .append("$expr", new Document("$gt", Arrays.asList(
                        new Document("$ifNull", Arrays.asList("$totalUsed", 0)),
                        0
                )));

        Update update = new Update()
                .inc("totalUsed", -1)
                .set("updatedAt", Instant.now());

        Coupon updated = mongoTemplate.findAndModify(
                new BasicQuery(filter),
                update,
                FindAndModifyOptions.options().returnNew(true),
                Coupon.class
        );

        return Optional.ofNullable(updated);
    }
}
