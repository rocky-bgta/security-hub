package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.enums.FeatureStatus;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.featureDto.RequestDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDtoWithPackageDetails;
import com.aspire.asat.cms.dto.featureDto.UpdateRequestDto;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.NullException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.Feature;
import com.aspire.asat.cms.repository.FeatureRepository;
import com.aspire.asat.cms.repository.PackageRepository;
import com.aspire.asat.cms.service.FeatureService;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;
    private final PackageRepository packageRepository;
    public FeatureServiceImpl(FeatureRepository featureRepository, PackageRepository packageRepository) {
        this.featureRepository = featureRepository;
        this.packageRepository = packageRepository;
    }

    @Override
    public ResponseDto saveFeature(RequestDto requestDto) {
        if(requestDto.getFeatureName() == null || requestDto.getFeatureName().isEmpty()){
            throw new NullException("Feature name cannot be null");
        }
        checkUniqueFeatureName(requestDto.getFeatureName());
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Feature feature = Feature.toFeature(id.toString(), requestDto, now, now);
        return Feature.toFeatureDto(featureRepository.save(feature));
    }

    @Override
    public List<ResponseDto> getAllFeatures(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        Page<Feature> pageFeature;
        if (search != null && !search.isEmpty()) {
            pageFeature = featureRepository.findByFeatureName(search, pageable);
        } else {
            pageFeature = featureRepository.findAll(pageable);
        }
        return pageFeature.getContent()
                .stream()
                .map(Feature::toFeatureDto)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseDtoWithPackageDetails getFeatureById(String id) {
        Feature feature = featureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: "+id));

//        TODO: remove this portion after converting all UUID to string
        List<String> stringPackageIds = new ArrayList<>();
        if (feature.getPackageIds() != null && !feature.getPackageIds().isEmpty()) {
            stringPackageIds = feature.getPackageIds()
                    .stream()
                    .map(String::toString)
                    .collect(Collectors.toList());
        }

        List<com.aspire.asat.cms.dto.packageDto.ResponseDto> packageDetails = packageRepository.findAllById(stringPackageIds)
                .stream()
                .map(packages -> com.aspire.asat.cms.dto.packageDto.ResponseDto.builder()
                        .id(packages.getId())
                        .packageName(packages.getPackageName())
                        .packageDescription(packages.getPackageDescription())
                        .packageStatus(packages.getPackageStatus())
                        .price(packages.getPrice())
                        .courseIds(packages.getCourseIds())
                        .featureIds(packages.getFeatureIds())
                        .createdAt(packages.getCreatedAt())
                        .updatedAt(packages.getUpdatedAt())
                        .lastModifiedBy(packages.getLastModifiedBy())
                        .build())
                .collect(Collectors.toList());
        return Feature.toFeatureDtoWithPackageDetails(feature, packageDetails);
    }

    @Override
    public ResponseDto updateFeatureById(String fretureId, UpdateRequestDto updateRequestDto) {
        String id = fretureId;
        return featureRepository.findById(id)
                .map(existingFeature -> {
                    if(updateRequestDto.getFeatureName() == null || updateRequestDto.getFeatureName().isEmpty()){
                        throw new NullException("Feature name cannot be null or empty");
                    }
                    if(updateRequestDto.getFeatureName() != null && !updateRequestDto.getFeatureName().equals(existingFeature.getFeatureName())){
                        checkUniqueFeatureName(updateRequestDto.getFeatureName());
                    }
                    Feature updatedFeature = Feature.toUpdateFeature(id, updateRequestDto);
                    updatedFeature.setCreatedAt(existingFeature.getCreatedAt());
                    return Feature.toFeatureDto(featureRepository.save(updatedFeature));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));
    }

    private void checkUniqueFeatureName(String featureName) {
        if (featureRepository.existsByFeatureName(featureName)) {
            throw new DuplicateNameException("Feature name '" + featureName + "' already exists.");
        }
    }


    @Override
    public String deleteFeatureById(String id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));

        String featureName = feature.getFeatureName();
        featureRepository.deleteById(id);

        return featureName;
    }

    @Override
    public void exportFeatures(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=feature.csv");

        List<ResponseDto> features = getAllFeatures(null,0, 1000, "createdAt", "desc");

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {

            String[] header = { "ID", "Feature Name", "Feature Description", "Status", "Package IDs", "Created At", "Updated At" };
            writer.writeNext(header);

            for (ResponseDto feature : features) {
                writer.writeNext(new String[]{
                        feature.getId().toString(),
                        feature.getFeatureName(),
                        feature.getFeatureDescription(),
                        feature.getFeatureStatus().toString(),
                        feature.getPackageIds().toString(),
                        feature.getCreatedAt().toString(),
                        feature.getUpdatedAt().toString()
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export features to CSV", e);
        }
    }

    @Override
    public long getTotalFeatureCount() {
        return featureRepository.count();
    }
    @Override
    public List<String> deleteFeaturesByIds(List<String> ids) {
        List<String> deletedNames = new ArrayList<>();
        for (String id : ids) {
            deletedNames.add(deleteFeatureById(id));
        }
        return deletedNames;
    }

    @Override
    public List<String> updateFeaturesStatusByIds(List<String> ids, Status status) {
        List<String> updatedFeatureNames = new ArrayList<>();
        for (String id : ids) {
            Feature feature = featureRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
            feature.setFeatureStatus(FeatureStatus.valueOf(status.name()));
            featureRepository.save(feature);
            updatedFeatureNames.add(feature.getFeatureName());
        }
        return updatedFeatureNames;
    }


    @Override
    public void exportBulkFeatures(List<String> ids, HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=bulk_features.csv");

        List<com.aspire.asat.cms.dto.featureDto.ResponseDto> features = ids.stream()
                .map(id -> {
                    Feature feature = featureRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + id));
                    return Feature.toFeatureDto(feature);
                })
                .collect(Collectors.toList());

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            String[] header = { "ID", "Feature Name", "Feature Description", "Status", "Package IDs", "Availability", "Created At", "Updated At" };
            writer.writeNext(header);

            for (com.aspire.asat.cms.dto.featureDto.ResponseDto tmpfeature : features) {
                writer.writeNext(new String[]{
                        tmpfeature.getId().toString(),
                        tmpfeature.getFeatureName(),
                        tmpfeature.getFeatureDescription(),
                        tmpfeature.getFeatureStatus().toString(),
                        tmpfeature.getPackageIds().toString(),
                        tmpfeature.getAvailability().toString(),
                        tmpfeature.getCreatedAt().toString(),
                        tmpfeature.getUpdatedAt().toString()
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export bulk features to CSV", e);
        }
    }
    @Override
    public List<ResponseDto> getAllFeaturesByStatus(String search, Integer offset, Integer pageSize, String sortBy, String order) {
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.fromString(order), sortBy));
        Page<Feature> features = featureRepository.findByFeatureStatus(FeatureStatus.ENABLED, pageable);
        List<ResponseDto> responseDtos = features.stream()
                .map(Feature::toFeatureDto)
                .collect(Collectors.toList());
        return responseDtos;
    }
}
