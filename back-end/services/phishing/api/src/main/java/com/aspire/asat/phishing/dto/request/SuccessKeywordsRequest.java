package com.aspire.asat.phishing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessKeywordsRequest {

    @Builder.Default
    private List<String> keywords = new ArrayList<>();
}
