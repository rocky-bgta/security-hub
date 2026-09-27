package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.enums.Role;
import com.aspire.asat.registration.data.UpdateUserDto;
import com.aspire.asat.registration.data.UserDto;
import com.aspire.asat.registration.data.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

import static com.aspire.asat.registration.data.enums.UserStatus.ACTIVE;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class User {

    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String designation;
    private Role role;
    private String group;
    private String department;
    private String country;
    private String zipCode;
    private String supervisorName;
    private String supervisorEmail;
    private UserStatus userStatus;
    private String logo;

    public static UserDto toUserDto(User user) {
        return UserDto.builder().
                id(user.getId()).
                firstName(user.getFirstName()).
                lastName(user.getLastName()).
                email(user.getEmail()).
                companyName(user.getCompanyName()).
                designation(user.getDesignation()).
                role(user.getRole()).
                group(user.getGroup()).
                department(user.getDepartment()).
                country(user.getCountry()).
                zipCode(user.getZipCode()).
                supervisorName(user.getSupervisorName()).
                supervisorEmail(user.getSupervisorEmail()).
                userStatus(user.getUserStatus()).
                logo(user.getLogo()).
                build();
    }

    public static User toUser(UUID id,String companyName,String no_profile, UserDto userDto) {
        return User.builder().
                id(id).
                firstName(userDto.getFirstName()).
                lastName(userDto.getLastName()).
                email(userDto.getEmail()).
                companyName(companyName).
                designation(userDto.getDesignation()).
                group(userDto.getGroup()).
                department(userDto.getDepartment()).
                country(userDto.getCountry()).
                zipCode(userDto.getZipCode()).
                supervisorName(userDto.getSupervisorName()).
                supervisorEmail(userDto.getSupervisorEmail()).
                userStatus(ACTIVE).
                logo(no_profile).
                build();
    }

    public static User toUpdateUser(UpdateUserDto updateUserserDto, String url) {
        return User.builder().
                id(updateUserserDto.getId()).
                firstName(updateUserserDto.getFirstName()).
                lastName(updateUserserDto.getLastName()).
                email(updateUserserDto.getEmail()).
                companyName(updateUserserDto.getCompanyName()).
                designation(updateUserserDto.getDesignation()).
                role(updateUserserDto.getRole()).
                group(updateUserserDto.getGroup()).
                department(updateUserserDto.getDepartment()).
                country(updateUserserDto.getCountry()).
                zipCode(updateUserserDto.getZipCode()).
                supervisorName(updateUserserDto.getSupervisorName()).
                supervisorEmail(updateUserserDto.getSupervisorEmail()).
                userStatus(updateUserserDto.getUserStatus()).
                logo(url).
                build();
    }

}