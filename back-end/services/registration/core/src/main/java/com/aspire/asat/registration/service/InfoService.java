package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.InfoDto;

import java.util.List;

public interface InfoService {

    InfoDto createInfo(InfoDto infoDto);

    ApiResponse<List<?>> getData(String type, String name);

}
