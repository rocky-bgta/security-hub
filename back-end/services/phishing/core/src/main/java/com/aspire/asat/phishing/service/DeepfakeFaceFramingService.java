package com.aspire.asat.phishing.service;

/**
 * Produces a consistently framed 16:9 head-and-shoulders JPEG from an uploaded
 * face photo, so the rendered deepfake video always shows the full face.
 */
public interface DeepfakeFaceFramingService {

    /**
     * Detects the primary face and crops/pads the image to a 16:9 frame with
     * headroom above the head and room for the shoulders below. Falls back to a
     * center crop when no face is detected or detection is unavailable.
     *
     * @param imageBytes raw PNG/JPEG bytes of the uploaded face photo
     * @return opaque JPEG bytes framed to 16:9
     */
    byte[] frameToLandscape(byte[] imageBytes);
}
