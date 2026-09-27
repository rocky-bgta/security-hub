package com.aspire.asat.universal.universal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDto {
    private UUID id;
    private String name;
    private String createdBy;
    private LocalDateTime createdAt;
}
