package com.aspire.asat.phishing.util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Sniffs face image formats from raw bytes. HeyGen photo avatars accept PNG/JPEG only.
 */
public final class DeepfakeImageUtils {

    public static final int MIN_DIMENSION = 128;
    public static final int MAX_DIMENSION = 4096;

    private DeepfakeImageUtils() {
    }

    public static final String JPEG = "image/jpeg";
    public static final String PNG = "image/png";

    /**
     * @return {@code image/jpeg} or {@code image/png} when supported; {@code null} otherwise
     */
    public static String detectSupportedFaceContentType(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return null;
        }
        if (isJpeg(bytes)) {
            return JPEG;
        }
        if (isPng(bytes)) {
            return PNG;
        }
        return null;
    }

    /**
     * @return {@code [width, height]} when decodable; {@code null} otherwise
     */
    public static int[] readImageDimensions(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                return null;
            }
            return new int[]{image.getWidth(), image.getHeight()};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decodes, validates HeyGen dimensions, converts to opaque RGB, and re-encodes as JPEG.
     */
    public static byte[] normalizeToJpegBytes(byte[] input) throws IOException {
        if (input == null || input.length == 0) {
            throw new IOException("Face image is empty");
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(input));
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new IOException("Unable to decode face image dimensions");
        }
        validateHeyGenDimensions(image.getWidth(), image.getHeight());
        return encodeJpeg(toOpaqueRgb(image));
    }

    /**
     * Re-encodes the face image as JPEG so HeyGen receives a file with explicit dimensions.
     */
    public static File normalizeToJpegFile(File input) throws IOException {
        byte[] jpegBytes = normalizeToJpegBytes(Files.readAllBytes(input.toPath()));
        File output = Files.createTempFile("heygen-face-", ".jpg").toFile();
        Files.write(output.toPath(), jpegBytes);
        return output;
    }

    public static void validateHeyGenDimensions(int width, int height) throws IOException {
        if (width < MIN_DIMENSION || height < MIN_DIMENSION) {
            throw new IOException("Face image dimensions must be at least "
                    + MIN_DIMENSION + "x" + MIN_DIMENSION + " pixels for HeyGen rendering");
        }
        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            throw new IOException("Face image dimensions must be at most "
                    + MAX_DIMENSION + "x" + MAX_DIMENSION + " pixels for HeyGen rendering");
        }
    }

    public static BufferedImage toOpaqueRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB
                && source.getTransparency() == BufferedImage.OPAQUE) {
            return source;
        }
        BufferedImage rgb = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return rgb;
    }

    public static byte[] encodeJpeg(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "jpg", output)) {
            throw new IOException("Unable to encode face image as JPEG");
        }
        return output.toByteArray();
    }

    public static boolean isAvif(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }
        if (bytes[4] != 'f' || bytes[5] != 't' || bytes[6] != 'y' || bytes[7] != 'p') {
            return false;
        }
        String majorBrand = new String(bytes, 8, 4);
        if ("avif".equals(majorBrand) || "avis".equals(majorBrand)) {
            return true;
        }
        for (int offset = 16; offset + 4 <= bytes.length && offset < 64; offset += 4) {
            String brand = new String(bytes, offset, 4);
            if ("avif".equals(brand) || "avis".equals(brand)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJpeg(byte[] bytes) {
        return bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] bytes) {
        return bytes.length >= 8
                && bytes[0] == (byte) 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47
                && bytes[4] == 0x0D
                && bytes[5] == 0x0A
                && bytes[6] == 0x1A
                && bytes[7] == 0x0A;
    }
}
