package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.NewsLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsLikeRepository extends MongoRepository<NewsLike, String> {
    Optional<NewsLike> findByNewsIdAndUserId(String newsId, String userId);
    int countByNewsIdAndLiked(String newsId, boolean liked);
}
