package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request to create micro content from an existing deepfake video.
 * {@code videoIds} must contain exactly one deepfake {@code renderId}.
 * Optional {@code topicName} overrides the default CMS topic name
 * ({@code Micro Content of {firstName}'s {videoTitle}}) when non-blank.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MicroContentCreateRequest {

    @NotEmpty
    @Size(min = 1, max = 1, message = "Exactly one videoId is required")
    private List<UUID> videoIds;

    /** Optional CMS topic name. Blank/null falls back to the formatted default. */
    private String topicName;
}
