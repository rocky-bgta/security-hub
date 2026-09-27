package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailsResponseDTO {

    @NotBlank
    private String courseId;

    @NotBlank
    private String courseName;

    private String courseDescription;

    private Instant publishDate;

    @NotNull
    private Double progress;

    @NotNull
    private Integer chapterCount;

    @NotNull
    private Integer contentCount;

    @NotNull
    private List<ChapterDetailsDTO> chapters;

    private String thumbnailUrl;

    private String certificateUrl;

    private String ImageCertificateLink;
}

