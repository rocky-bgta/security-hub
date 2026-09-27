package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.ClientDto;
import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.ClientRequestDto;
import com.aspire.asat.registration.data.ClientUpdateDto;
import com.aspire.asat.registration.model.Client;
import com.aspire.asat.registration.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequestMapping(value = WebApiUrlConstants.CLIENT_API, produces = "application/json")
public interface ClientController {

    @GetMapping
    ResponseEntity<List<ClientDto>> getAllClients(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ClientDto> getClientById(UUID id);

    @PutMapping
    ResponseEntity<ClientDto> updateClient(@ModelAttribute ClientUpdateDto clientUpdateDto) throws IOException;

    @PostMapping
    ResponseEntity<ClientDto> createClient(@ModelAttribute ClientRequestDto requestDto) throws IOException;

    @PostMapping(WebApiUrlConstants.BULK_CLIENT_API)
    ResponseEntity<List<String>> createClients(@RequestBody List<ClientDto> clientDtos);

    @GetMapping(WebApiUrlConstants.PATH_VAR_SEARCH)
    List<Client> searchClients(@RequestParam String query);

    @GetMapping(WebApiUrlConstants.COMPANY_NAME)
    List<User> getUsersByCompanyName(@PathVariable String companyName);

    @PostMapping(WebApiUrlConstants.PATH_VAR_UPLOAD)
    ResponseEntity<List<String>> uploadCsvFile(@RequestParam("file") MultipartFile file) throws IOException;

}