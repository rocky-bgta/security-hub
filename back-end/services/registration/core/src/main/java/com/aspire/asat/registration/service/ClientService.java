package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.ClientDto;
import com.aspire.asat.registration.data.ClientRequestDto;
import com.aspire.asat.registration.data.ClientUpdateDto;
import com.aspire.asat.registration.model.Client;
import com.aspire.asat.registration.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ClientService {

    List<ClientDto> getAllClients(Integer offset, Integer pageSize);

    ClientDto getClientById(UUID id);

    ClientDto updateClient(ClientUpdateDto clientUpdateDto) throws IOException;

    ClientDto saveClient(ClientRequestDto requestDto) throws IOException;

    List<String> saveClients(List<ClientDto> clientDto);

    List<Client> searchWithRelevance(String text);

    List<User> getUsersByCompanyName(String companyName);

    List<String> saveCsvToDatabase(MultipartFile file) throws IOException;

}