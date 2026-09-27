package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.InfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.aspire.asat.registration.constant.WebApiUrlConstants.INFO_API;

@RequestMapping(value = INFO_API, produces = "application/json")
public interface InfoController {

    @PostMapping
    ResponseEntity<InfoDto> createEntity(@RequestBody InfoDto infoDto);

    @GetMapping
    ResponseEntity<ApiResponse<List<?>>> getData(@RequestParam String type, @RequestParam String name);

    //Delete - Update

}
