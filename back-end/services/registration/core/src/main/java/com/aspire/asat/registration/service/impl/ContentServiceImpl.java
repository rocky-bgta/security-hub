package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.ContentCountDto;
import com.aspire.asat.registration.data.ContentDto;
import com.aspire.asat.registration.exception.*;
import com.aspire.asat.registration.model.Content;
import com.aspire.asat.registration.repository.ContentRepository;
import com.aspire.asat.registration.service.ContentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    public static final String CONTENT_IS_NULL = "Content is null";
    private final MongoTemplate mongoTemplate;

    public ContentServiceImpl(ContentRepository contentRepository, MongoTemplate mongoTemplate) {
        this.contentRepository = contentRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ContentDto save(ContentDto saveContentDto) {
        validateContentDto(saveContentDto);
        Content content = new Content();
        content = contentRepository.save(content.toContent(saveContentDto));
        return content.toContentDto(content);
    }

    @Override
    public List<ContentDto> getAllContent(Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Content> pageContent = contentRepository.findAll(pageable);
        List<Content> allContent = pageContent.getContent();
        return allContent.stream().map(content -> content.toContentDto(content)).collect(Collectors.toList());
    }

    public void validateContentDto(ContentDto saveContentDto) {

        RegistationValidationException registationValidationException = new RegistationValidationException();

        if (saveContentDto.getName() == null) {
            registationValidationException.addValidationException(new InvalidContentNameException("Name cannot be null"));
        }

        if (saveContentDto.getType() == null) {
            registationValidationException.addValidationException(new InvalidContentTypeException("Type cannot be null"));
        }

        if (!registationValidationException.getAllValidationException().isEmpty()) {
            throw registationValidationException;
        }
    }

    @Override
    public ApiResponse<List<ContentCountDto>> getTypeCount() {

        GroupOperation groupByType = group("type").count().as("value");

        ProjectionOperation projectToRename = project("value").and("_id").as("name");

        Aggregation aggregation = newAggregation(groupByType, projectToRename);

        AggregationResults<ContentCountDto> results = mongoTemplate.aggregate(aggregation, "content", ContentCountDto.class);

        List<ContentCountDto> contentCountList = results.getMappedResults();

        return new ApiResponse<>("Data fetched successfully", HttpStatus.OK.value(), contentCountList);

    }

    @Override
    public ContentDto updateContent(ContentDto toBeUpdate) {
        if (!contentRepository.existsById(toBeUpdate.getId())) {
            throw new ContentDoesNotExistException(CONTENT_IS_NULL);
        }
        Content updatedContent = contentRepository.save(Content.toUpdateContent(toBeUpdate));
        return Content.toContentDto(updatedContent);
    }

}