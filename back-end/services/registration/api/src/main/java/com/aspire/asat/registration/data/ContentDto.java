package com.aspire.asat.registration.data;

import com.aspire.asat.registration.data.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ContentDto {

    private UUID id;
    private String name;
    private ContentType type;

}

