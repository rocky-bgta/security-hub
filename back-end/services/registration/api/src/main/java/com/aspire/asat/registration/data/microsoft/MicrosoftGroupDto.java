package com.aspire.asat.registration.data.microsoft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicrosoftGroupDto {
    private String id;
    private String displayName;
    private String description;
    private String mailNickname;
    private int memberCount;
    private List<MicrosoftUserDto> members;
}

