package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.Exams;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends MongoRepository<Exams, String> {

    Optional<Exams> findBySubPackageId(String packageId);

    Optional<Exams> findByExamId(String examId);

    // Exam results retrieval (supports multiple attempts per user+subPackage, e.g. resume flow).
    // When only one exam exists, these behave like the previous single-result API (backward compatible).
    /** Returns the latest exam (by createdAt) for the user and sub-package; use for resume and display. */
    Optional<Exams> findFirstByUserIdAndSubPackageIdOrderByCreatedAtDesc(String userId, String subPackageId);

    /** Returns all exams for the user and sub-package, newest first (e.g. for reset-all). */
    List<Exams> findByUserIdAndSubPackageIdOrderByCreatedAtDesc(String userId, String subPackageId);

    /** True if the user has ever passed an exam for this sub-package (any attempt). */
    boolean existsByUserIdAndSubPackageIdAndExamPassedTrue(String userId, String subPackageId);

    List<Exams> findByUserId(String userId);

    List<Exams> findBySubPackageIdAndExamPassed(String subPackageId, boolean passed);

    List<Exams> findByClientAdminId(String clientAdminId);

}
