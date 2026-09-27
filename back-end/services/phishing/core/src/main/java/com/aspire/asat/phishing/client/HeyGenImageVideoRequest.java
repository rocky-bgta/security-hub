package com.aspire.asat.phishing.client;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class HeyGenImageVideoRequest {
    String imageAssetId;
    String imageUrl;
    byte[] imageBytes;
    String imageMediaType;
    String audioUrl;
    String audioAssetId;
    String title;
    String resolution;
    String aspectRatio;
    String expressiveness;
    String motionPrompt;
    Map<String, Object> background;
    boolean removeBackground;
}
