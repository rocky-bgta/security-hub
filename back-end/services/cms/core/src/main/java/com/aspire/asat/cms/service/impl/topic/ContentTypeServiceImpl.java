package com.aspire.asat.cms.service.impl.topic;

import com.aspire.asat.cms.dto.topic.ContentTypeReqDto;
import com.aspire.asat.cms.dto.topic.ContentTypeRespDto;
import com.aspire.asat.cms.exception.DuplicateDataFoundException;
import com.aspire.asat.cms.model.topic.ContentType;
import com.aspire.asat.cms.repository.topic.ContentTypeRepository;
import com.aspire.asat.cms.service.topic.ContentTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentTypeServiceImpl implements ContentTypeService {

    private final ContentTypeRepository contentTypeRepository;

    @Override
    public ContentTypeRespDto createContentType(ContentTypeReqDto contentTypeReqDto) {
        log.info( "Creating content type: {}", contentTypeReqDto);
        if(contentTypeRepository.existsByTypeNameIgnoreCase(contentTypeReqDto.getTypeName())){
            throw new DuplicateDataFoundException("Content type with name '" + contentTypeReqDto.getTypeName() + "' already exists.");
        }
        ContentType contentTypeToSave = ContentType.toContentType(contentTypeReqDto);
        return ContentType.contentTypeRespDto(contentTypeRepository.save(contentTypeToSave));
    }

    @Override
    public ContentTypeRespDto updateContentType(String id, ContentTypeReqDto contentTypeReqDto) {
       log.info( "Updating content type with id: {}", id);
       ContentType existingContentType = contentTypeRepository.findById(id)
               .orElseThrow(() -> new DuplicateDataFoundException("Content type with id '" + id + "' not found."));

         if(!existingContentType.getTypeName().equalsIgnoreCase(contentTypeReqDto.getTypeName())
                    && contentTypeRepository.existsByTypeNameIgnoreCase(contentTypeReqDto.getTypeName())){
                throw new DuplicateDataFoundException("Content type with name '" + contentTypeReqDto.getTypeName() + "' already exists.");
            }
            updateContentTypeData(existingContentType, contentTypeReqDto);
            return ContentType.contentTypeRespDto(contentTypeRepository.save(existingContentType));
    }



    @Override
    public ContentTypeRespDto getById(String id) {
        log.info( "Fetching content type with id: {}", id);
        ContentType contentType = contentTypeRepository.findById(id)
                .orElseThrow(() -> new DuplicateDataFoundException("Content type with id '" + id + "' not found."));
        return ContentType.contentTypeRespDto(contentType);
    }

    @Override
    public void deleteContentTypeById(String id) {
        log.info( "Deleting content type with id: {}", id);
        if(!contentTypeRepository.existsById(id)){
            throw new DuplicateDataFoundException("Content type with id '" + id + "' not found.");
        }
        contentTypeRepository.deleteById(id);
    }

    @Override
    public List<ContentTypeRespDto> getAllContentTypes() {
        log.info( "Fetching all content types");
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        List<ContentType> contentTypes = contentTypeRepository.findAll(sort);
        return contentTypes.stream()
                .map(ContentType::contentTypeRespDto)
                .toList();
    }

    private void updateContentTypeData(ContentType existingContentType, ContentTypeReqDto contentTypeReqDto) {
        if(contentTypeReqDto .getTypeName() != null){
            existingContentType.setTypeName(contentTypeReqDto.getTypeName());
        }
        if(contentTypeReqDto.getDescription() != null){
            existingContentType.setDescription(contentTypeReqDto.getDescription());
        }
        if(contentTypeReqDto.getSortOrder() != null){
            existingContentType.setSortOrder(contentTypeReqDto.getSortOrder());
        }
        existingContentType.setUpdatedAt(java.time.Instant.now());

    }
}
