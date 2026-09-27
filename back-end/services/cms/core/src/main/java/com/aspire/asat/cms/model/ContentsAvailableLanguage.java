package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageReqDto;
import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageRespDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "Contents_Available_Language")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentsAvailableLanguage {

    private String id;
    private String languageName;
    private String code;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static ContentsAvailableLanguage toEntity(ContentsAvailableLanguageReqDto dto) {
        return ContentsAvailableLanguage.builder()
                .id(UUID.randomUUID().toString())
                .languageName(dto.getLanguageName())
                .code(dto.getCode())
                .active(dto.isActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static ContentsAvailableLanguageRespDto toRespDto(ContentsAvailableLanguage entity) {
        return ContentsAvailableLanguageRespDto.builder()
                .id(entity.getId())
                .languageName(entity.getLanguageName())
                .code(entity.getCode())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
