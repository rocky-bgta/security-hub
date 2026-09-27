package com.aspire.asat.universal.service;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.entity.LatestNews;
import com.aspire.asat.universal.entity.NewsLike;
import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.category.CategoryDto;
import com.aspire.asat.universal.news.LatestNewsDto;
import com.aspire.asat.universal.news.LatestNewsRequest;
import com.aspire.asat.universal.news.NewsSequenceRequest;
import com.aspire.asat.universal.repository.CategoryRepository;
import com.aspire.asat.universal.repository.LatestNewsRepository;
import com.aspire.asat.universal.repository.NewsLikeRepository;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.utils.UserTypeUtils;
import com.aspire.asat.universal.entity.Category;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;

@Service
public class LatestNewsService {

    private final LatestNewsRepository latestNewsRepository;
    private final CategoryRepository categoryRepository;
    private final NewsLikeRepository newsLikeRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final ModelMapper modelMapper;
    private final NewsCommentService newsCommentService;

    @Autowired
    public LatestNewsService(
            LatestNewsRepository latestNewsRepository,
            CategoryRepository categoryRepository,
            NewsLikeRepository newsLikeRepository,
            UserCurrentContextService userCurrentContextService,
            ModelMapper modelMapper,
            NewsCommentService newsCommentService) {
        this.latestNewsRepository = latestNewsRepository;
        this.categoryRepository = categoryRepository;
        this.newsLikeRepository = newsLikeRepository;
        this.userCurrentContextService = userCurrentContextService;
        this.modelMapper = modelMapper;
        this.newsCommentService = newsCommentService;
    }

    public Page<LatestNewsDto> getAllNews(Pageable pageable) {
        return latestNewsRepository.findAll(pageable)
                .map(news -> convertToDto(news, false));
    }

    public Page<LatestNewsDto> getActiveNews(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return latestNewsRepository.findByStatusAndPublishedDateBeforeAndExpireDateAfterOrderBySequenceAsc(
                Status.ACTIVE, now, now, pageable)
                .map(news -> convertToDto(news, false));
    }

    // New: offset-based list (items only) + separate count methods
    public List<LatestNewsDto> getAllNews(int offset, int pageSize) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        Pageable pageable = PageRequest.of(offset, pageSize);
        return latestNewsRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(news -> convertToDto(news, false))
                .collect(Collectors.toList());
    }

    public long getAllNewsCount() {
        return latestNewsRepository.count();
    }

    public List<LatestNewsDto> getActiveNews(int offset, int pageSize) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        Pageable pageable = PageRequest.of(offset, pageSize);
        LocalDateTime now = LocalDateTime.now();
        return latestNewsRepository.findByStatusAndPublishedDateBeforeAndExpireDateAfterOrderBySequenceAsc(
                Status.ACTIVE, now, now, pageable)
                .getContent()
                .stream()
                .map(news -> convertToDto(news, false))
                .collect(Collectors.toList());
    }

    public long getActiveNewsCount() {
        LocalDateTime now = LocalDateTime.now();
        return latestNewsRepository.countByStatusAndPublishedDateBeforeAndExpireDateAfter(Status.ACTIVE, now, now);
    }

    public LatestNewsDto getNewsById(String id) {
        Optional<LatestNews> news = latestNewsRepository.findById(id);
        return news.map(n -> convertToDto(n, true)).orElse(null);
    }

    public LatestNewsDto getNewsBySlug(String slug) {
        Optional<LatestNews> news = latestNewsRepository.findBySlug(slug);
        return news.map(n -> convertToDto(n, true)).orElse(null);
    }

    public LatestNewsDto createNews(LatestNewsRequest request) {
        denyIfMspAdmin("created");
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        // Validate slug uniqueness
        if (request.getSlug() != null && !request.getSlug().isEmpty()) {
            Optional<LatestNews> existingNews = latestNewsRepository.findBySlug(request.getSlug());
            if (existingNews.isPresent()) {
                throw new IllegalArgumentException("News with slug '" + request.getSlug() + "' already exists");
            }
        }

        // Validate category exists and format
        if (request.getCategoryId() == null || request.getCategoryId().isEmpty()) {
            throw new IllegalArgumentException("Category ID is required");
        }

        // Ensure categoryId is a valid ObjectId (prevents arbitrary strings from being treated as existing IDs)
        if (!ObjectId.isValid(request.getCategoryId())) {
            throw new IllegalArgumentException("Invalid category ID format: " + request.getCategoryId());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + request.getCategoryId()));

        // Manually map fields from request to entity to avoid accidental id/category collisions by ModelMapper
        LatestNews news = new LatestNews();
        news.setName(request.getName());
        news.setSlug(request.getSlug());
        news.setCategoryId(request.getCategoryId());
        news.setContent(request.getContent());
        news.setSequence(request.getSequence());
        news.setImageUrl(request.getImageUrl());
        news.setVideoUrl(request.getVideoUrl());
        news.setPublishedDate(request.getPublishedDate());
        news.setExpireDate(request.getExpireDate());
        news.setStatus(request.getStatus());
        // attach resolved category
        news.setCategory(category);
        news.setCreatedBy(context.getUserId());
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedBy(context.getUserId());
        news.setUpdatedAt(LocalDateTime.now());
        news.setLikeCount(0);
        news.setDislikeCount(0);

        LatestNews savedNews = latestNewsRepository.save(news);
        return convertToDto(savedNews, true);
    }

    public LatestNewsDto updateNews(String id, LatestNewsRequest request) {
        denyIfMspAdmin("updated");
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        // Validate slug uniqueness (if changing the slug)
        if (request.getSlug() != null && !request.getSlug().isEmpty()) {
            Optional<LatestNews> existingNewsBySlug = latestNewsRepository.findBySlug(request.getSlug());
            if (existingNewsBySlug.isPresent() && !existingNewsBySlug.get().getId().toString().equals(id)) {
                throw new IllegalArgumentException("News with slug '" + request.getSlug() + "' already exists");
            }
        }

        // Validate category exists
        if (request.getCategoryId() == null || request.getCategoryId().isEmpty()) {
            throw new IllegalArgumentException("Category ID is required");
        }
        if (!ObjectId.isValid(request.getCategoryId())) {
            throw new IllegalArgumentException("Invalid category ID format: " + request.getCategoryId());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + request.getCategoryId()));

        Optional<LatestNews> existingNewsOptional = latestNewsRepository.findById(id);
        if (existingNewsOptional.isPresent()) {
            LatestNews existingNews = existingNewsOptional.get();

            // Manually update fields from request (avoid ModelMapper to prevent id collisions)
            existingNews.setName(request.getName());
            existingNews.setSlug(request.getSlug());
            existingNews.setCategoryId(request.getCategoryId());
            existingNews.setContent(request.getContent());
            existingNews.setSequence(request.getSequence());
            existingNews.setImageUrl(request.getImageUrl());
            existingNews.setVideoUrl(request.getVideoUrl());
            existingNews.setPublishedDate(request.getPublishedDate());
            existingNews.setExpireDate(request.getExpireDate());
            existingNews.setStatus(request.getStatus());
            existingNews.setCategory(category);

            //existingNews.setId(id); // Ensure ID is not changed
            existingNews.setUpdatedBy(context.getUserId());
            existingNews.setUpdatedAt(LocalDateTime.now());

            LatestNews updatedNews = latestNewsRepository.save(existingNews);
            return convertToDto(updatedNews, true);
        }
        return null;
    }

    public boolean deleteNews(String id) {
        denyIfMspAdmin("deleted");
        if (latestNewsRepository.existsById(id)) {
            latestNewsRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public LatestNewsDto updateNewsSequence(NewsSequenceRequest request) {
        denyIfMspAdmin("modified");
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        Optional<LatestNews> existingNewsOptional = latestNewsRepository.findById(request.getId());
        if (existingNewsOptional.isPresent()) {
            LatestNews existingNews = existingNewsOptional.get();
            existingNews.setSequence(request.getSequence());
            existingNews.setUpdatedBy(context.getUserId());
            existingNews.setUpdatedAt(LocalDateTime.now());

            LatestNews updatedNews = latestNewsRepository.save(existingNews);
            return convertToDto(updatedNews, true);
        }
        return null;
    }

    public LatestNewsDto likeNews(String newsId, boolean liked) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String userId = context.getUserId();

        Optional<LatestNews> newsOptional = latestNewsRepository.findById(newsId);
        if (newsOptional.isEmpty()) {
            return null;
        }

        LatestNews news = newsOptional.get();
        Optional<NewsLike> existingLikeOptional = newsLikeRepository.findByNewsIdAndUserId(newsId, userId);

        if (existingLikeOptional.isPresent()) {
            NewsLike existingLike = existingLikeOptional.get();

            // If the action is the same, no change needed
            if (existingLike.isLiked() == liked) {
                return convertToDto(news, false);
            }

            // Remove previous like/dislike count
            if (existingLike.isLiked()) {
                news.setLikeCount(Math.max(0, news.getLikeCount() - 1));
            } else {
                news.setDislikeCount(Math.max(0, news.getDislikeCount() - 1));
            }

            // Update with new preference
            existingLike.setLiked(liked);
            existingLike.setCreatedAt(LocalDateTime.now());
            newsLikeRepository.save(existingLike);

        } else {
            // Create new like record
            NewsLike newLike = new NewsLike();
            newLike.setNewsId(newsId);
            newLike.setUserId(userId);
            newLike.setLiked(liked);
            newLike.setCreatedAt(LocalDateTime.now());
            newsLikeRepository.save(newLike);
        }

        // Update the count
        if (liked) {
            news.setLikeCount(news.getLikeCount() + 1);
        } else {
            news.setDislikeCount(news.getDislikeCount() + 1);
        }

        LatestNews updatedNews = latestNewsRepository.save(news);
        return convertToDto(updatedNews, false);
    }

    // Update only the status field; kept as a separate method so controllers can update status without providing full payload
    public LatestNewsDto updateNewsStatus(String id, Status status) {
        denyIfMspAdmin("modified");
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        Optional<LatestNews> existingNewsOptional = latestNewsRepository.findById(id);
        if (existingNewsOptional.isPresent()) {
            LatestNews existingNews = existingNewsOptional.get();
            existingNews.setStatus(status);
            existingNews.setUpdatedBy(context != null ? context.getUserId() : null);
            existingNews.setUpdatedAt(LocalDateTime.now());
            LatestNews updated = latestNewsRepository.save(existingNews);
            return convertToDto(updated, false);
        }
        return null;
    }

    // New: return OffsetPageDto for all news (moves controller pagination logic into service)
    public OffsetPageDto<LatestNewsDto> getAllNewsPage(int offset, int pageSize, String status, String categoryId, String search) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        // Treat offset as page number (0-based)
        Pageable pageable = PageRequest.of(offset, pageSize);
        
        Page<LatestNews> newsPage = latestNewsRepository.findAllWithFilters(status, categoryId, search, pageable);
        
        List<LatestNewsDto> items = newsPage.getContent()
                .stream()
                .map(news -> convertToDto(news, false))
                .collect(Collectors.toList());
        
        long total = newsPage.getTotalElements();
        
        return new OffsetPageDto<>(offset, pageSize, total, items);
    }

    // New: return OffsetPageDto for active news
    public OffsetPageDto<LatestNewsDto> getActiveNewsPage(int offset, int pageSize, String status, String categoryId, String search) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;

        Pageable pageable = PageRequest.of(offset, pageSize);
        LocalDateTime now = LocalDateTime.now();
        
        Page<LatestNews> newsPage = latestNewsRepository.findActiveWithFilters(status, categoryId, search, now, pageable);
        
        List<LatestNewsDto> items = newsPage.getContent()
                .stream()
                .map(news -> convertToDto(news, false))
                .collect(Collectors.toList());

        // Get current user context
        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();
        String userId = ctx != null ? ctx.getUserId() : null;

        // Add user like status for each news item
        if (userId != null) {
            for (LatestNewsDto dto : items) {
                Optional<NewsLike> userLike = newsLikeRepository.findByNewsIdAndUserId(dto.getId().toString(), userId);
                if (userLike.isPresent()) {
                    dto.setUserLikeStatus(userLike.get().isLiked());
                } else {
                    dto.setUserLikeStatus(null);
                }
            }
        } else {
            // No user logged in, set all to null
            for (LatestNewsDto dto : items) {
                dto.setUserLikeStatus(null);
            }
        }

        long total = newsPage.getTotalElements();
        return new OffsetPageDto<>(offset, pageSize, total, items);
    }

    private void denyIfMspAdmin(String operation) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        if (context != null && UserTypeUtils.isMspAdmin(context.getUserType())) {
            throw new IllegalArgumentException("Latest news cannot be " + operation + " by MSP");
        }
    }

    private LatestNewsDto convertToDto(LatestNews news, boolean includeComments) {
        LatestNewsDto dto = new LatestNewsDto();

        // Manually map fields to avoid ModelMapper ambiguity
        dto.setId(news.getId());
        dto.setName(news.getName());
        dto.setSlug(news.getSlug());
        dto.setCategoryId(news.getCategoryId());
        dto.setContent(news.getContent());
        dto.setSequence(news.getSequence());
        dto.setImageUrl(news.getImageUrl());
        dto.setVideoUrl(news.getVideoUrl());
        dto.setPublishedDate(news.getPublishedDate());
        dto.setExpireDate(news.getExpireDate());
        dto.setStatus(news.getStatus());
        dto.setLikeCount(news.getLikeCount());
        dto.setDislikeCount(news.getDislikeCount());

        // Convert category to DTO if present
        if (news.getCategory() != null) {
            CategoryDto categoryDto = modelMapper.map(news.getCategory(), CategoryDto.class);
            dto.setCategory(categoryDto);
        }

        if (includeComments) {
            try {
                dto.setComments(newsCommentService.getCommentsForNews(news.getId()));
            } catch (Exception ex) {
                // swallow comment errors to avoid breaking news retrieval; log in future
            }
        }

        return dto;
    }
}
