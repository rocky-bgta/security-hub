package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.exam.ExamQuestion;
import com.aspire.asat.cms.dto.exam.PackageExamCreateDto;
import com.aspire.asat.cms.dto.exam.PackageExamListResponseDto;
import com.aspire.asat.cms.dto.exam.PackageExamResponseDto;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.service.PackageExamService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageExamServiceImpl implements PackageExamService {

    @Autowired
    private ExamRepository repository;
    @Autowired
    private UserPackageRepositoryCustom customRepo;

    @Override
    public List<PackageExamResponseDto> getAllExams(int offset, int limit) {
        return repository.findAll()
                .stream()
                .skip(offset)
                .limit(limit)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalExamCount() {
        return repository.count();
    }

    @Override
    public PackageExamResponseDto updateExam(String id, PackageExamCreateDto dto) {
        Exams exam = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package exam not found with id: " + id));

        exam.setTitle(dto.getTitle());
        exam.setPassingScore(dto.getPassingScore());
        exam.setExamDetails(dto.getExamDetails() );

        Exams updated = repository.save(exam);
        return mapToDto(updated);
    }

    private PackageExamResponseDto mapToDto(Exams exam) {
        return PackageExamResponseDto.builder()
                .id(exam.getExamId())
                .packageId(exam.getSubPackageId())
                .title(exam.getTitle())
                .passingScore(exam.getPassingScore())
                .build();
    }

    @Override
    public List<PackageExamListResponseDto> searchExams(String search, int offset, int limit) {
        List<Document> docs = customRepo.searchExamsWithPackageName(search, offset, limit);
        List<PackageExamListResponseDto> responseList = new ArrayList<>();

        for (Document doc : docs) {
            PackageExamListResponseDto dto = new PackageExamListResponseDto();
            dto.setExamId(doc.getString("examId")); // comes from `_id` mapped as `examId` in project()
            dto.setPackageId(doc.getString("packageId"));
            dto.setPackageName(doc.getString("packageName"));
            dto.setTitle(doc.getString("title"));
            dto.setExamDetails(doc.getString("examDetails"));
            Object scoreObj = doc.get("passingScore");
            if (scoreObj instanceof Number) {
                dto.setPassingScore(((Number) scoreObj).doubleValue());
            }
            dto.setQuestions((List<ExamQuestion>) doc.get("questions"));


            responseList.add(dto);
        }

        return responseList;
    }


    @Override
    public long getTotalExamSearchCount(String search) {
        return customRepo.countExamsWithSearch(search);
    }



}
