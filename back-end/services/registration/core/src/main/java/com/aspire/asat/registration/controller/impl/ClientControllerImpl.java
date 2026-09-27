package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.data.ClientDto;
import com.aspire.asat.registration.controller.ClientController;
import com.aspire.asat.registration.data.ClientRequestDto;
import com.aspire.asat.registration.data.ClientUpdateDto;
import com.aspire.asat.registration.model.Client;
import com.aspire.asat.registration.model.User;
import com.aspire.asat.registration.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
public class ClientControllerImpl implements ClientController {

    private final ClientService clientService;

    public ClientControllerImpl(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public ResponseEntity<ClientDto> createClient(@ModelAttribute ClientRequestDto requestDto) throws IOException {
        ClientDto savedClient = clientService.saveClient(requestDto);
        return new ResponseEntity<>(savedClient, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<String>> createClients(@RequestBody List<ClientDto> clientDtos){
        List<String> unregistered = clientService.saveClients(clientDtos);
        return new ResponseEntity<>(unregistered,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ClientDto>> getAllClients(@RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset, @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        List<ClientDto> clients = clientService.getAllClients(offset, pageSize);
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ClientDto> getClientById(@PathVariable("id") UUID id) {
        ClientDto clientDto = clientService.getClientById(id);
        return new ResponseEntity<>(clientDto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ClientDto> updateClient(@ModelAttribute ClientUpdateDto clientUpdateDto) throws IOException {
            ClientDto clientDto = clientService.updateClient(clientUpdateDto);
            return new ResponseEntity<>(clientDto,HttpStatus.OK);
    }

    @Override
    public List<Client> searchClients(@RequestParam String query) {
        return clientService.searchWithRelevance(query);
    }

    @Override
    public List<User> getUsersByCompanyName(@PathVariable String companyName) {
        return clientService.getUsersByCompanyName(companyName);
    }

    @Override
    public ResponseEntity<List<String>> uploadCsvFile(@RequestParam("file") MultipartFile file) throws IOException {
        List<String> clientDtos = clientService.saveCsvToDatabase(file);
        return new ResponseEntity<>(clientDtos, HttpStatus.OK);
    }

}