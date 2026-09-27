package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.ContentCountDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.STATS_API, produces = "application/json")
public interface StatsController {

    @GetMapping
    ResponseEntity<ApiResponse<List<ContentCountDto>>> getTypeCount();

}
