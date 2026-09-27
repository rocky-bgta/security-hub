package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.DeepfakeFaceFramingService;
import com.aspire.asat.phishing.util.DeepfakeImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;
import software.amazon.awssdk.services.rekognition.model.Image;

import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;

/**
 * Frames uploaded face photos to a consistent 16:9 head-and-shoulders shot using
 * AWS Rekognition face detection. When no face is found or detection fails, it
 * falls back to a center crop so the render pipeline is never blocked.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeepfakeFaceFramingServiceImpl implements DeepfakeFaceFramingService {

    private static final int TARGET_WIDTH = 1920;
    private static final int TARGET_HEIGHT = 1080;
    private static final double TARGET_ASPECT = (double) TARGET_WIDTH / TARGET_HEIGHT;

    /** Extra space above the detected face, as a multiple of the face height. */
    private static final double HEADROOM_RATIO = 0.7;
    /** Space below the face (chin + shoulders), as a multiple of the face height. */
    private static final double SHOULDER_RATIO = 1.6;

    /** Rekognition inline image byte limit is 5MB; downscale a copy before detection if larger. */
    private static final int REKOGNITION_MAX_BYTES = 5 * 1024 * 1024;
    private static final int DETECTION_MAX_DIMENSION = 2048;

    private final RekognitionClient rekognitionClient;

    @Override
    public byte[] frameToLandscape(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ServiceException("Face image is empty", HttpStatus.BAD_REQUEST);
        }
        BufferedImage source = decode(imageBytes);

        Rect crop = detectFaceCrop(source, imageBytes)
                .orElseGet(() -> centerCrop(source.getWidth(), source.getHeight()));

        BufferedImage framed = renderToTarget(source, crop);
        try {
            return DeepfakeImageUtils.encodeJpeg(framed);
        } catch (IOException e) {
            throw new ServiceException("Unable to encode framed face image", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private BufferedImage decode(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new ServiceException("Unable to decode face image", HttpStatus.BAD_REQUEST);
            }
            return image;
        } catch (IOException e) {
            throw new ServiceException("Unable to decode face image", HttpStatus.BAD_REQUEST, e);
        }
    }

    private java.util.Optional<Rect> detectFaceCrop(BufferedImage source, byte[] imageBytes) {
        try {
            byte[] detectionBytes = detectionBytes(source, imageBytes);
            DetectFacesResponse response = rekognitionClient.detectFaces(DetectFacesRequest.builder()
                    .image(Image.builder().bytes(SdkBytes.fromByteArray(detectionBytes)).build())
                    .attributes(Attribute.DEFAULT)
                    .build());

            BoundingBox box = response.faceDetails().stream()
                    .map(FaceDetail::boundingBox)
                    .filter(b -> b != null && b.width() != null && b.height() != null)
                    .max(Comparator.comparingDouble(b -> b.width() * b.height()))
                    .orElse(null);
            if (box == null) {
                log.warn("No face detected in uploaded photo; falling back to center crop");
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(faceCrop(source.getWidth(), source.getHeight(), box));
        } catch (Exception e) {
            log.warn("Rekognition face detection failed; falling back to center crop", e);
            return java.util.Optional.empty();
        }
    }

    private byte[] detectionBytes(BufferedImage source, byte[] imageBytes) throws IOException {
        if (imageBytes.length <= REKOGNITION_MAX_BYTES) {
            return imageBytes;
        }
        double scale = Math.min(1.0,
                (double) DETECTION_MAX_DIMENSION / Math.max(source.getWidth(), source.getHeight()));
        int w = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        var g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(scaled, "jpg", out);
        return out.toByteArray();
    }

    private Rect faceCrop(int width, int height, BoundingBox box) {
        double faceX = box.left() * width;
        double faceY = box.top() * height;
        double faceW = box.width() * width;
        double faceH = box.height() * height;

        double topY = faceY - HEADROOM_RATIO * faceH;
        double bottomY = faceY + faceH + SHOULDER_RATIO * faceH;
        double cropH = bottomY - topY;
        double cropW = cropH * TARGET_ASPECT;
        double centerX = faceX + faceW / 2.0;
        double cropX = centerX - cropW / 2.0;
        return new Rect(cropX, topY, cropW, cropH);
    }

    private Rect centerCrop(int width, int height) {
        double imageAspect = (double) width / height;
        if (imageAspect > TARGET_ASPECT) {
            double cropH = height;
            double cropW = cropH * TARGET_ASPECT;
            return new Rect((width - cropW) / 2.0, 0, cropW, cropH);
        }
        double cropW = width;
        double cropH = cropW / TARGET_ASPECT;
        return new Rect(0, (height - cropH) / 2.0, cropW, cropH);
    }

    /**
     * Draws the source into a 1920x1080 white canvas so the crop rectangle maps
     * to the full frame. Regions of the crop that fall outside the source stay
     * white (letterbox/pillarbox) rather than distorting the image.
     */
    private BufferedImage renderToTarget(BufferedImage source, Rect crop) {
        BufferedImage target = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);
        var g = target.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, TARGET_WIDTH, TARGET_HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double scale = TARGET_WIDTH / crop.w();
        AffineTransform transform = new AffineTransform();
        transform.translate(-crop.x() * scale, -crop.y() * scale);
        transform.scale(scale, scale);
        g.drawImage(DeepfakeImageUtils.toOpaqueRgb(source), transform, null);
        g.dispose();
        return target;
    }

    private record Rect(double x, double y, double w, double h) {
    }
}
