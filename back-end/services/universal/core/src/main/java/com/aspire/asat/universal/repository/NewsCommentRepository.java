package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.NewsComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NewsCommentRepository extends MongoRepository<NewsComment, UUID> {
    List<NewsComment> findByNewsIdAndParentCommentIdIsNullOrderByCreatedAtAsc(UUID newsId);
    List<NewsComment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
    List<NewsComment> findByNewsIdOrderByCreatedAtAsc(UUID newsId);
}

