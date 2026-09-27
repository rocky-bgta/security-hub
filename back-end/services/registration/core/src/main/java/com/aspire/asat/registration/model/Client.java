package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.ClientDto;
import com.aspire.asat.registration.data.ClientRequestDto;
import com.aspire.asat.registration.data.enums.ClientRole;
import com.aspire.asat.registration.data.enums.ClientStatus;
import com.aspire.asat.registration.data.ClientUpdateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Client {

    @Id
    private UUID id;
    private String name;
    private ClientRole role;
    private String type;
    private String email;
    private String domain;
    private String phone;
    private String language;
    private String country;
    private String timeZone;
    private String zipCode;
    private String size;
    private ClientStatus clientStatus;
    private String industry;
    private String techName;
    private String techEmail;
    private String billingName;
    private String billingEmail;
    private String address;
    private String logo;

    public static ClientDto toClientDto(Client client) {
        return ClientDto.builder().
                id(client.getId()).
                name(client.getName()).
                role(client.getRole()).
                type(client.getType()).
                email(client.getEmail()).
                domain(client.getDomain()).
                phone(client.getPhone()).
                language(client.getLanguage()).
                country(client.getCountry()).
                timeZone(client.getTimeZone()).
                zipCode(client.getZipCode()).
                size(client.getSize()).
                clientStatus(client.getClientStatus()).
                industry(client.getIndustry()).
                techName(client.getTechName()).
                techEmail(client.getTechEmail()).
                billingName(client.getBillingName()).
                billingEmail(client.getBillingEmail()).
                address(client.getAddress()).
                logo(client.getLogo()).
                build();
    }

    public static Client toClient(UUID clientId, ClientRequestDto clientDto, String logoUrl) {
        return Client.builder().
                id(clientId).
                name(clientDto.getName()).
                role(clientDto.getRole()).
                type(clientDto.getType()).
                email(clientDto.getEmail()).
                domain(clientDto.getDomain()).
                phone(clientDto.getPhone()).
                language(clientDto.getLanguage()).
                country(clientDto.getCountry()).
                timeZone(clientDto.getTimeZone()).
                zipCode(clientDto.getZipCode()).
                size(clientDto.getSize()).
                clientStatus(clientDto.getClientStatus()).
                industry(clientDto.getIndustry()).
                techName(clientDto.getTechName()).
                techEmail(clientDto.getTechEmail()).
                billingName(clientDto.getBillingName()).
                billingEmail(clientDto.getBillingEmail()).
                address(clientDto.getAddress()).
                logo(logoUrl).
                build();
    }

    public static Client toClientFromCsv(UUID clientId, ClientDto clientDto, String logoUrl) {
        return Client.builder().
                id(clientId).
                name(clientDto.getName()).
                email(clientDto.getEmail()).
                domain(clientDto.getDomain()).
                country(clientDto.getCountry()).
                type(clientDto.getType()).
                role(clientDto.getRole()).
                logo(logoUrl).
                build();
    }

    public static Client toUpdateClient(ClientUpdateDto clientDto, String url) {
        return Client.builder().
                id(clientDto.getId()).
                name(clientDto.getName()).
                role(clientDto.getRole()).
                type(clientDto.getType()).
                email(clientDto.getEmail()).
                domain(clientDto.getDomain()).
                phone(clientDto.getPhone()).
                language(clientDto.getLanguage()).
                country(clientDto.getCountry()).
                timeZone(clientDto.getTimeZone()).
                zipCode(clientDto.getZipCode()).
                size(clientDto.getSize()).
                clientStatus(clientDto.getClientStatus()).
                industry(clientDto.getIndustry()).
                techName(clientDto.getTechName()).
                techEmail(clientDto.getTechEmail()).
                billingName(clientDto.getBillingName()).
                billingEmail(clientDto.getBillingEmail()).
                address(clientDto.getAddress()).
                logo(url).
                build();
    }

}