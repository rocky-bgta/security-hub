package com.aspire.asat.cms.dto.bookmark;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkRequestDto {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Topic ID is required")
    private String topicId;

    @NotBlank(message = "Sub Package ID is required")
    private String subPackageId;

    private boolean isBookmarked = true; // default is true
}
