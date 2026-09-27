package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.LinkContentDto;
import com.aspire.asat.cms.dto.enums.ContentType;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.NullException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.LinkContent;
import com.aspire.asat.cms.repository.LinkContentRepository;
import com.aspire.asat.cms.service.LinkContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LinkContentServiceImpl implements LinkContentService {

    private final LinkContentRepository linkContentRepository;
    private static final String URL_REGEX = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    @Autowired
    public LinkContentServiceImpl(LinkContentRepository linkContentRepository) {
        this.linkContentRepository = linkContentRepository;
    }

    @Override
    public ContentRespDto<LinkContentDto> createLinkContent(ContentReqDto<LinkContentDto> linkContentDto) {

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        if(!isValidName(linkContentDto.getCommon().getContentName())){
            throw new NullException("Cannot be null and minimum length should be 2 "+linkContentDto.getCommon().getContentName());
        }
//        if(!isValidUrl(linkContentDto.getCommon().getUrl())){
//            throw new NullException("Url in not valid "+linkContentDto.getCommon().getUrl());
//        }
        if(linkContentRepository.existsByContentName(linkContentDto.getCommon().getContentName())){
            throw new DuplicateNameException("Link Content already exists with name: " + linkContentDto.getCommon().getContentName());
        }

        LinkContent linkContent = LinkContent.toLinkContent(id.toString(), linkContentDto, now, now);
        return LinkContent.toContentRespDto(linkContentRepository.save(linkContent));
    }

    @Override
    public ContentRespDto<LinkContentDto> updateLinkContent(UUID contentId, ContentReqDto<LinkContentDto> linkContentDto) {
        return linkContentRepository.findById(contentId)
                .map(existingLinkContent -> {
                    if(!isValidName(linkContentDto.getCommon().getContentName())){
                        throw new NullException("Cannot be null and minimum length should be 2 "+linkContentDto.getCommon().getContentName());
                    }
//                    if(!isValidUrl(linkContentDto.getCommon().getUrl())){
//                        throw new NullException("Url in not valid "+linkContentDto.getCommon().getUrl());
//                    }
                    if(!existingLinkContent.getContentName().equals(linkContentDto.getCommon().getContentName())){
                        boolean isContentNameExist = linkContentRepository.existsByContentName(linkContentDto.getCommon().getContentName());
                        if(isContentNameExist){
                            throw new DuplicateNameException("Link Content already exists with name: " + linkContentDto.getCommon().getContentName());
                        }
                    }
                    LinkContent updatedLinkContent = LinkContent.toUpdateLinkContent(existingLinkContent, linkContentDto, Instant.now());
                    return LinkContent.toContentRespDto(linkContentRepository.save(updatedLinkContent));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Link Content not found with id: " + contentId));
    }

    @Override
    public List<ContentRespDto<LinkContentDto>> getAllLinkContents(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));

        Page<LinkContent> pageLinkContent;

        if (search != null && !search.isEmpty()) {
            pageLinkContent = linkContentRepository.findByContentNameContainingIgnoreCaseAndContentType(search, ContentType.LINK, pageable);
        } else {
            pageLinkContent = linkContentRepository.findByContentType(ContentType.LINK, pageable);
        }

        return pageLinkContent.getContent()
                .stream()
                .map(LinkContent::toContentRespDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContentRespDto<LinkContentDto> getLinkContentById(UUID contentId) {
        return linkContentRepository.findById(contentId)
                .map(LinkContent::toContentRespDto)
                .orElseThrow(() -> new ResourceNotFoundException("Link Content not found with id: " + contentId));
    }

    @Override
    public ContentRespDto<LinkContentDto> deleteLinkContentById(UUID contentId) {
        return linkContentRepository.findById(contentId)
                .map(linkContent -> {
                    linkContentRepository.delete(linkContent);
                    return LinkContent.toContentRespDto(linkContent);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Link Content not found with id: " + contentId));
    }

    @Override
    public long getTotalLinkCount() {
        return linkContentRepository.countByContentType(ContentType.LINK);
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }
    public boolean isValidName(String title) {
        if (title == null || title.length() < 2) {
            return false;
        }
        return Character.isLetter(title.charAt(0));
    }

}
