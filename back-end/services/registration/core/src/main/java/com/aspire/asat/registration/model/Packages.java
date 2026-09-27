package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.PackagesDto;
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
@NoArgsConstructor
@AllArgsConstructor

public class Packages {

    @Id
    private UUID id;
    private String title;
    private String description;

    public static PackagesDto toPackagesDto(Packages packages) {
        return PackagesDto.builder().
                id(packages.getId()).
                title(packages.getTitle()).
                description(packages.getDescription()).
                build();
    }

    public static Packages toPackages(PackagesDto packagesDto) {
        return Packages.builder().
                id(UUID.randomUUID()).
                title(packagesDto.getTitle()).
                description(packagesDto.getDescription()).
                build();
    }

    public static Packages toUpdatePackages(PackagesDto toBeUpdate) {
        return Packages.builder().
                id(toBeUpdate.getId()).
                title(toBeUpdate.getTitle()).
                description(toBeUpdate.getDescription()).
                build();
    }

}
