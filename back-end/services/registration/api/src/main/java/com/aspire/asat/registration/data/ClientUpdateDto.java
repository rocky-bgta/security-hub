package com.aspire.asat.registration.data;

import com.aspire.asat.registration.data.enums.ClientRole;
import com.aspire.asat.registration.data.enums.ClientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ClientUpdateDto {

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
    private MultipartFile png;

}