package com.aspire.asat.cms.dto.language;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentsAvailableLanguageRespDto {

    private String id;
    private String languageName;
    private String code;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
