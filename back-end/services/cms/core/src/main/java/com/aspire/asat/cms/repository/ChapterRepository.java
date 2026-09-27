package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChapterRepository extends MongoRepository<Chapter, String> {

    boolean existsByChapterName(String chapterName);

    @Query("{ 'courseName': { $regex: ?0, $options: 'i' } }")
    Page<Chapter> findByChapterName(String text, Pageable pageable);

    List<Chapter> findAllByChapterName(String text);

    Page<Chapter> findByTopicId(String topicId, Pageable pageable);

    long countByTopicId(String topicId);

    List<Chapter> findByTopicId(String topicId);

    boolean existsByChapterNameAndTopicId(String chapterName, String topicId);
}
