package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientCourseResponseDTO {

    @NotBlank
    private String courseId;

    @NotBlank
    private String courseName;

    @NotNull
    @Min(0)
    private Integer totalContentCount;

    @NotNull
    @Min(0)
    private Integer completedContentCount;

    @NotNull
    @Min(0)
    private Integer chapterCount;

    @NotNull
    private Double progress; // percentage (0.0 to 100.0)

    @NotBlank
    private String status; // e.g., "not_started", "in_progress", "completed"

    private boolean isSaved; // nullable

    private String certificateLink; // nullable

    private boolean expired;

    private String thumbnailUrl;

    private Instant createdAt;
}
