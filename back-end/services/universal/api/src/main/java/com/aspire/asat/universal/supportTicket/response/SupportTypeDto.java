package com.aspire.asat.universal.supportTicket.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTypeDto {
    @Schema(description = "Support type identifier", example = "COURSE_ENROLLMENT_PROBLEM")
    private String id;

    @Schema(description = "Support type name", example = "Course Enrollment Problem")
    private String name;
}
