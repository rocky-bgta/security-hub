package com.aspire.asat.cms.dto.interactive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhishingEmailDTO {
    private String id;
    private String senderName;
    private String senderEmail;
    private String subject;
    private String date;
    private String time;
    private String avatar;
    private String body;
    private List<PhishingEmailLinkDTO> links;

    @JsonProperty("isPhishing")
    private Boolean isPhishing;

    private String explanation;
}
