package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.InfoController;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.InfoDto;
import com.aspire.asat.registration.service.InfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InfoControllerImpl implements InfoController {

    private final InfoService infoService;

    public InfoControllerImpl(InfoService infoService) {
        this.infoService = infoService;
    }

    @Override
    public ResponseEntity<InfoDto> createEntity(@RequestBody InfoDto infoDto) {
        InfoDto info = infoService.createInfo(infoDto);
        return new ResponseEntity<>(info, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponse<List<?>>> getData(@RequestParam String type, @RequestParam String name) {
        ApiResponse<List<?>> list = infoService.getData(type, name);
        return new ResponseEntity<>(list, HttpStatus.CREATED);
    }

}
