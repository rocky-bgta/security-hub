package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.ContentCountDto;

import java.util.List;

public interface StatsService {

    ApiResponse<List<ContentCountDto>> getTypeCount();

}
