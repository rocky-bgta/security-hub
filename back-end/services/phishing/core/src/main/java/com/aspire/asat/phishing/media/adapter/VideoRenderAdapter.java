package com.aspire.asat.phishing.media.adapter;

import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import com.aspire.asat.phishing.media.model.VideoRenderInput;
import com.aspire.asat.phishing.media.model.VideoRenderResult;

/**
 * Provider-agnostic contract for fusing a face image + audio into a video.
 */
public interface VideoRenderAdapter {

    /**
     * @return the provider this adapter handles, or {@code null} for a fallback adapter
     */
    VideoRenderProvider getProvider();

    /**
     * Fallback adapters handle any provider that has no dedicated adapter registered.
     */
    default boolean isFallback() {
        return false;
    }

    VideoRenderResult render(VideoRenderInput input);
}
