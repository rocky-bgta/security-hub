package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.StatsController;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.ContentCountDto;
import com.aspire.asat.registration.service.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatsControllerImpl implements StatsController {

    private final StatsService statsService;

    public StatsControllerImpl(StatsService statsService) {
        this.statsService = statsService;
    }

    @Override
    public ResponseEntity<ApiResponse<List<ContentCountDto>>> getTypeCount() {
        ApiResponse<List<ContentCountDto>> stats = statsService.getTypeCount();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

}
