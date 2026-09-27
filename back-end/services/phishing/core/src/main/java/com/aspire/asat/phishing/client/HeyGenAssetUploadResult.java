package com.aspire.asat.phishing.client;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class HeyGenAssetUploadResult {
    String assetId;
    String url;
    String mimeType;
}
