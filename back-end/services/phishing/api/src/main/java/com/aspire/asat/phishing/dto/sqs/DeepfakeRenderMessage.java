package com.aspire.asat.phishing.dto.sqs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepfakeRenderMessage {
    private String renderId;
    private String bucket;
}
