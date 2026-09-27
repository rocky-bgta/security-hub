package com.aspire.asat.phishing.media;

import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundPreset;
import com.aspire.asat.phishing.dto.enums.DeepfakeBackgroundType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class HeyGenBackgroundMapper {

    @Value("${heygen.backgrounds.office:}")
    private String officeBackgroundUrl;

    @Value("${heygen.backgrounds.newsroom:}")
    private String newsroomBackgroundUrl;

    @Value("${heygen.backgrounds.studio:}")
    private String studioBackgroundUrl;

    @Value("${heygen.backgrounds.boardroom:}")
    private String boardroomBackgroundUrl;

    @Value("${heygen.backgrounds.greenscreen:#00B140}")
    private String greenscreenColor;

    @Value("${heygen.backgrounds.gradient:#1a1a2e}")
    private String gradientColor;

    public Map<String, Object> toHeyGenBackground(DeepfakeBackgroundType type,
                                                  DeepfakeBackgroundPreset preset,
                                                  String customImageUrl) {
        if (type == DeepfakeBackgroundType.CUSTOM) {
            if (customImageUrl == null || customImageUrl.isBlank()) {
                return null;
            }
            return imageBackground(customImageUrl);
        }
        if (type != DeepfakeBackgroundType.PRESET || preset == null) {
            return null;
        }
        return switch (preset) {
            case OFFICE -> presetImageOrNull(officeBackgroundUrl, "office");
            case NEWSROOM -> presetImageOrNull(newsroomBackgroundUrl, "newsroom");
            case STUDIO -> presetImageOrNull(studioBackgroundUrl, "studio");
            case BOARDROOM -> presetImageOrNull(boardroomBackgroundUrl, "boardroom");
            case GREENSCREEN -> colorBackground(greenscreenColor);
            case GRADIENT -> colorBackground(gradientColor);
        };
    }

    private Map<String, Object> presetImageOrNull(String url, String presetName) {
        if (url == null || url.isBlank()) {
            log.warn("HeyGen preset background '{}' has no URL configured; omitting background", presetName);
            return null;
        }
        return imageBackground(url);
    }

    private Map<String, Object> imageBackground(String url) {
        Map<String, Object> background = new LinkedHashMap<>();
        background.put("type", "image");
        background.put("url", url);
        return background;
    }

    private Map<String, Object> colorBackground(String color) {
        Map<String, Object> background = new LinkedHashMap<>();
        background.put("type", "color");
        background.put("value", normalizeColor(color));
        return background;
    }

    private String normalizeColor(String color) {
        if (color == null || color.isBlank()) {
            return "#000000";
        }
        String trimmed = color.trim();
        if (trimmed.startsWith("#")) {
            return trimmed;
        }
        return "#" + trimmed.toUpperCase(Locale.ROOT);
    }
}
