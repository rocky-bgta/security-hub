package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.ClientDto;
import com.aspire.asat.registration.data.ClientRequestDto;
import com.aspire.asat.registration.data.enums.ClientRole;
import com.aspire.asat.registration.data.ClientUpdateDto;
import com.aspire.asat.registration.data.CompanyDto;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.exception.ClientDoesNotExistException;
import com.aspire.asat.registration.exception.EmailAlreadyExistsException;
import com.aspire.asat.registration.exception.EmailNotValidException;
import com.aspire.asat.registration.model.Client;
import com.aspire.asat.registration.model.Company;
import com.aspire.asat.registration.model.User;
import com.aspire.asat.registration.repository.ClientRepository;
import com.aspire.asat.registration.repository.CompanyRepository;
import com.aspire.asat.registration.repository.UserRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.BlobStorageService;
import com.aspire.asat.registration.service.ClientService;
import com.aspire.asat.registration.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.aspire.asat.registration.constant.WebApiUrlConstants.BATCH_SIZE;
import static com.aspire.asat.registration.constant.WebApiUrlConstants.CLIENT_LOGO;
import static com.aspire.asat.registration.constant.WebApiUrlConstants.DAYS;
import static com.aspire.asat.registration.constant.WebApiUrlConstants.NO_PROFILE;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

@Service
@Slf4j
public class ClientServiceImpl implements ClientService {

    public static final String CLIENT_IS_NULL = "Client is null";
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final BlobStorageService blobStorageService;
    private final CompanyRepository companyRepository;
    private final AspireUserService aspireUserService;
    private static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String EMAIL_NOT_VALID = "Email Not Valid";

    @Autowired
    private MongoTemplate mongoTemplate;

    public ClientServiceImpl(ClientRepository clientRepository, UserRepository userRepository, CompanyRepository companyRepository, BlobStorageService blobStorageService, AspireUserService aspireUserService) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.blobStorageService = blobStorageService;
        this.companyRepository = companyRepository;
        this.aspireUserService = aspireUserService;
    }

    @Override
    public List<ClientDto> getAllClients(Integer offset, Integer pageSize) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Client> pageClient = clientRepository.findAll(pageable);
        List<Client> allClient = pageClient.getContent();
        return allClient.stream().map(client -> client.toClientDto(client)).collect(Collectors.toList());
    }

    @Override
    public ClientDto saveClient(ClientRequestDto requestDto) throws IOException {
        if (!isValidEmail(requestDto.getEmail())) throw new EmailNotValidException(EMAIL_NOT_VALID);
        if (checkEmailExists(requestDto.getEmail())) throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS);
        UUID id = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        String logoUrl = blobStorageService.uploadLogo(CLIENT_LOGO, id, requestDto.getLogo());
        Client client = Client.toClient(id, requestDto, logoUrl);
        Company company = Company.toCompany(companyId, requestDto.getName(), requestDto.getDomain());
        CompanyDto companyDto = Company.toCompanyDto(companyRepository.save(company));
        Client savedClient = clientRepository.save(client);

        // Create AspireUser record for centralized user management
        try {
            aspireUserService.createAspireUser(
                    savedClient.getId().toString(),
                    UserType.CLIENT,
                    savedClient
            );
            log.info("Created AspireUser record for Client with ID: {}", savedClient.getId());
        } catch (Exception e) {
            log.error("Failed to create AspireUser record for Client with ID: {}", savedClient.getId(), e);
            // Don't fail the main operation if AspireUser creation fails
        }

        return Client.toClientDto(savedClient);
    }

    @Override
    public List<String> saveClients(List<ClientDto> clientDtos) {
        List<String> nonRegisteredList = new ArrayList<>();
        for (ClientDto clientDto : clientDtos) {
            try {
                if (!isValidEmail(clientDto.getEmail())) throw new EmailNotValidException(EMAIL_NOT_VALID);
                if (checkEmailExists(clientDto.getEmail())) throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS);
                Client client = Client.toClientFromCsv(UUID.randomUUID(), clientDto, NO_PROFILE);
                clientRepository.save(client);
            } catch (Exception e) {
                nonRegisteredList.add(clientDto.getEmail());
            }
        }
        return nonRegisteredList;
    }

    @Override
    public ClientDto getClientById(UUID id) {
        Optional<Client> clientFromDb = clientRepository.findById(id);
        if (clientFromDb.isEmpty()) {
            throw new ClientDoesNotExistException(CLIENT_IS_NULL);
        }
        return Client.toClientDto(clientFromDb.get());
    }

    @Override
    public ClientDto updateClient(ClientUpdateDto clientUpdateDto) throws IOException {
        Optional<Client> clientFromDb = clientRepository.findById(clientUpdateDto.getId());
        if (clientFromDb.isEmpty()) {
            throw new ClientDoesNotExistException(CLIENT_IS_NULL);
        }
        String url = blobStorageService.uploadLogo(CLIENT_LOGO, clientUpdateDto.getId(), clientUpdateDto.getPng());
        Client updatedClient = clientRepository.save(Client.toUpdateClient(clientUpdateDto, url));

        // Update AspireUser record for centralized user management
        try {
            aspireUserService.updateAspireUser(
                    updatedClient.getId().toString(),
                    UserType.CLIENT,
                    updatedClient
            );
            log.info("Updated AspireUser record for Client with ID: {}", updatedClient.getId());
        } catch (Exception e) {
            log.error("Failed to update AspireUser record for Client with ID: {}", updatedClient.getId(), e);
            // Don't fail the main operation if AspireUser update fails
        }

        return Client.toClientDto(updatedClient);
    }

    @Override
    public List<Client> searchWithRelevance(String text) {
        return clientRepository.findByTextSearch(text);
    }

    @Override
    public List<User> getUsersByCompanyName(String companyName) {
        return userRepository.findUsersByCompanyName(companyName);
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public boolean checkEmailExists(String email) {
        Aggregation aggregation = Aggregation.newAggregation(
                match(org.springframework.data.mongodb.core.query.Criteria.where("email").is(email)),
                project().andExclude("_id").andExpression("true").as("exists")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "client", Document.class);
        List<Document> mappedResults = results.getMappedResults();
        return !mappedResults.isEmpty();
    }

    @Override
    public List<String> saveCsvToDatabase(MultipartFile file) throws IOException {
        List<String> nonRegisteredList = new ArrayList<String>();
        List<Client> batch = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                try {
                    UUID id = UUID.randomUUID();
                    ClientDto clientDto = ClientDto.builder()
                            .id(id)
                            .email(record.get("email"))
                            .domain(record.get("domain"))
                            .country(record.get("country"))
                            .type(record.get("type"))
                            .role(ClientRole.USER)
                            .build();
                    if (!isValidEmail(clientDto.getEmail())) throw new EmailNotValidException(EMAIL_NOT_VALID);
                    if (checkEmailExists(clientDto.getEmail()))
                        throw new EmailAlreadyExistsException(EMAIL_ALREADY_EXISTS);
                    Client client = clientRepository.save(Client.toClientFromCsv(id, clientDto, NO_PROFILE));

                    // Create AspireUser record for centralized user management
                    try {
                        aspireUserService.createAspireUser(
                                client.getId().toString(),
                                UserType.CLIENT,
                                client
                        );
                        log.info("Created AspireUser record for Client from CSV with ID: {}", client.getId());
                    } catch (Exception e) {
                        log.error("Failed to create AspireUser record for Client from CSV with ID: {}", client.getId(), e);
                        // Don't fail the main operation if AspireUser creation fails
                    }

                    String token = JwtUtil.generateToken(client, DAYS);
                    batch.add(client);

                    if (batch.size() == BATCH_SIZE) {
                        clientRepository.saveAll(batch);
                        batch.clear();
                    }
                } catch (Exception e) {
                    nonRegisteredList.add(record.get("email"));
                }
            }
            if (!batch.isEmpty()) {
                clientRepository.saveAll(batch);
                batch.clear();
            }
        }
        return nonRegisteredList;
    }

}