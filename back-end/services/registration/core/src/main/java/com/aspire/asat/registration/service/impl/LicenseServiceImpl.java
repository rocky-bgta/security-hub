package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.LicenseCountDto;
import com.aspire.asat.registration.data.LicenseDto;
import com.aspire.asat.registration.exception.LessonDoesNotExistException;
import com.aspire.asat.registration.model.License;
import com.aspire.asat.registration.repository.LicenseRepository;
import com.aspire.asat.registration.service.LicenseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LicenseServiceImpl implements LicenseService {

    public static final String LICENSE_IS_NULL = "License is null";
    private final LicenseRepository licenseRepository;
    private MongoTemplate mongoTemplate;

    public LicenseServiceImpl(LicenseRepository licenseRepository, MongoTemplate mongoTemplate) {
        this.licenseRepository = licenseRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public LicenseDto save(LicenseDto licenseDto) {
        return License.toLicenseDto(licenseRepository.save(License.toLicense(licenseDto)));
    }

    @Override
    public List<LicenseDto> getAllLicense(Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<License> pageLicnese = licenseRepository.findAll(pageable);
        List<License> allClient = pageLicnese.getContent();
        return allClient.stream().map(license -> license.toLicenseDto(license)).collect(Collectors.toList());
    }

    @Override
    public LicenseDto updateLicense(LicenseDto toBeUpdate) {
        if (!licenseRepository.existsById(toBeUpdate.getLicenseId())) {
            throw new LessonDoesNotExistException(LICENSE_IS_NULL);
        }
        License updatedLicense = licenseRepository.save(License.toUpdateLicense(toBeUpdate));
        return License.toLicenseDto(updatedLicense);
    }

    public long licenseCount() {
        return licenseRepository.count();
    }

    public long countValidLicenses() {

        Date currentDate = new Date();

        AggregationOperation match = Aggregation.match(Criteria.where("issueDate").lte(currentDate).and("expireDate").gte(currentDate));

        Aggregation aggregation = Aggregation.newAggregation(match);

        AggregationResults<License> results = mongoTemplate.aggregate(aggregation, "license", License.class);

        return results.getMappedResults().size();

    }

    public long countExpiredLicenses() {

        Date currentDate = new Date();

        AggregationOperation match = Aggregation.match(Criteria.where("expireDate").lt(currentDate));

        Aggregation aggregation = Aggregation.newAggregation(match);

        AggregationResults<License> results = mongoTemplate.aggregate(aggregation, "license", License.class);

        return results.getMappedResults().size();

    }

    public long countLicensesExpiring() {

        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, 90);
        Date dateAfter90Days = calendar.getTime();

        AggregationOperation match = Aggregation.match(Criteria.where("expireDate").gte(currentDate).lte(dateAfter90Days));

        Aggregation aggregation = Aggregation.newAggregation(match);

        AggregationResults<License> results = mongoTemplate.aggregate(aggregation, "license", License.class);

        return results.getMappedResults().size();
    }

    @Override
    public ApiResponse<List<LicenseCountDto>> getAllLicenseCount() {
        LicenseCountDto licenseCountDto = new LicenseCountDto();
        licenseCountDto.setName(mongoTemplate.getCollectionName(License.class));
        licenseCountDto.setTotalLicense(licenseCount());
        licenseCountDto.setOnBoarded(countValidLicenses());
        licenseCountDto.setExpired(countExpiredLicenses());
        licenseCountDto.setToBeExpired(countLicensesExpiring());
        return new ApiResponse<>("Data fetched successfully", HttpStatus.OK.value(), Arrays.asList(licenseCountDto));
    }

}