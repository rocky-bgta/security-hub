package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.question.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewQuestionRepository extends MongoRepository<Question, String> {
    // Basic CRUD operations are inherited from MongoRepository
    // All complex filtering is handled by QuestionCustomRepository
}
