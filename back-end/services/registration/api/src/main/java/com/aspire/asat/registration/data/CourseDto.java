package com.aspire.asat.registration.data;

import com.aspire.asat.registration.data.enums.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CourseDto {

    private UUID id;
    private String title;
    private CourseStatus status;
    private String description;

}
