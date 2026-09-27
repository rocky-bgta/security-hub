package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.ContentCountDto;
import com.aspire.asat.registration.repository.ClientRepository;
import com.aspire.asat.registration.repository.CourseRepository;
import com.aspire.asat.registration.repository.LessonRepository;
import com.aspire.asat.registration.repository.PackagesRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ClientRepository clientRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final PackagesRepository packagesRepository;
    private final MspUsersRepository mspUsersRepository;


    @Override
    public ApiResponse<List<ContentCountDto>> getTypeCount() {

        List<ContentCountDto> contentCountList = Arrays.asList(
                new ContentCountDto("Client", clientRepository.count()),
                new ContentCountDto("Msp", mspUsersRepository.count()),
                new ContentCountDto("Package", packagesRepository.count()),
                new ContentCountDto("Lesson", lessonRepository.count()),
                new ContentCountDto("Course", courseRepository.count()));

        return new ApiResponse<>("Data fetched successfully", HttpStatus.OK.value(), contentCountList);

    }

}
