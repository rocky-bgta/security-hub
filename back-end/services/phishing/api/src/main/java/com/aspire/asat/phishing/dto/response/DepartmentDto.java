package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Embedded department reference on email templates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {

    @Field("id")
    private String id;
    private String name;
}
