package com.aspire.asat.universal.repository;

import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class PollSurveyVoteLogRepositoryImpl implements PollSurveyVoteLogRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public PollSurveyVoteLogRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Object[]> countVotesByPollSurveyId(UUID pollSurveyId) {
        MatchOperation match = Aggregation.match(Criteria.where("pollSurveyId").is(pollSurveyId));
        GroupOperation group = Aggregation.group("answerId").count().as("cnt");
        Aggregation agg = Aggregation.newAggregation(match, group);
        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "poll_survey_vote_logs", Document.class);
        List<Object[]> out = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Object idObj = doc.get("_id");
            UUID answerId = null;
            if (idObj instanceof UUID) {
                answerId = (UUID) idObj;
            } else if (idObj instanceof String) {
                try {
                    answerId = UUID.fromString((String) idObj);
                } catch (IllegalArgumentException ignored) {
                }
            } else if (idObj instanceof Binary) {
                try {
                    byte[] bytes = ((Binary) idObj).getData();
                    ByteBuffer bb = ByteBuffer.wrap(bytes);
                    long high = bb.getLong();
                    long low = bb.getLong();
                    answerId = new UUID(high, low);
                } catch (Exception ignored) {
                }
            } else if (idObj instanceof byte[]) {
                try {
                    byte[] bytes = (byte[]) idObj;
                    ByteBuffer bb = ByteBuffer.wrap(bytes);
                    long high = bb.getLong();
                    long low = bb.getLong();
                    answerId = new UUID(high, low);
                } catch (Exception ignored) {
                }
            } else {
                // fallback: try to parse toString
                try {
                    answerId = UUID.fromString(idObj.toString());
                } catch (Exception ignored) {
                }
            }

            Number cntNum = doc.get("cnt", Number.class);
            long cnt = cntNum != null ? cntNum.longValue() : 0L;
            out.add(new Object[]{answerId, cnt});
        }
        return out;
    }
}

