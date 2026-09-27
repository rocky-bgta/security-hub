package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.InfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Info {

    @Id
    private UUID id;
    private String type;
    private String name;
    private List<String> timezones;

    public static InfoDto toInfoDto(Info info) {
        return InfoDto.builder().
                id(info.getId()).
                type(info.getType()).
                name(info.getName()).
                timezones(info.getTimezones()).
                build();
    }

    public static Info toInfo(InfoDto infoDto) {
        return Info.builder().
                id(UUID.randomUUID()).
                type(infoDto.getType()).
                name(infoDto.getName()).
                timezones(infoDto.getTimezones()).
                build();
    }
}
