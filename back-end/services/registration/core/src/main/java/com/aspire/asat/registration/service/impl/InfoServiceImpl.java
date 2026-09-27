package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.InfoDto;
import com.aspire.asat.registration.data.NameProjectionDto;
import com.aspire.asat.registration.data.TimeZoneProjectionDto;
import com.aspire.asat.registration.model.Info;
import com.aspire.asat.registration.repository.InfoRepository;
import com.aspire.asat.registration.service.InfoService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfoServiceImpl implements InfoService {

    private final InfoRepository infoRepository;
    private final MongoTemplate mongoTemplate;

    public InfoServiceImpl(InfoRepository infoRepository, MongoTemplate mongoTemplate) {
        this.infoRepository = infoRepository;
        this.mongoTemplate  = mongoTemplate;
    }

    @Override
    public InfoDto createInfo(InfoDto infoDto) {
        return Info.toInfoDto(infoRepository.save(Info.toInfo(infoDto)));
    }

    @Override
    public ApiResponse<List<?>> getData(String type, String name) {
        if(name.isEmpty()) {
            MatchOperation matchStage = Aggregation.match(Criteria.where("type").is(type));
            Aggregation aggregation = Aggregation.newAggregation(matchStage);
            List<?> entities = mongoTemplate.aggregate(aggregation, "info", NameProjectionDto.class).getMappedResults();
            return new ApiResponse<>("Data fetched successfully", HttpStatus.OK.value(), entities);
        }

        MatchOperation matchStage = Aggregation.match(
                Criteria.where("name").is(name).and("type").is(type)
        );

        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        List<?> entities = mongoTemplate.aggregate(aggregation, "info", TimeZoneProjectionDto.class).getMappedResults();
        return new ApiResponse<>("Data fetched successfully", HttpStatus.OK.value(), entities);

    }
}
