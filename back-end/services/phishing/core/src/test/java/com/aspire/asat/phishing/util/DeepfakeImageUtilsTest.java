package com.aspire.asat.phishing.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepfakeImageUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void normalizeToJpegBytes_argbPng_producesOpaqueJpeg() throws IOException {
        byte[] argbPng = writePng(createArgbImage(256, 256));

        byte[] jpeg = DeepfakeImageUtils.normalizeToJpegBytes(argbPng);

        assertTrue(jpeg.length > 0);
        assertTrue(isJpegContent(jpeg));
        BufferedImage decoded = ImageIO.read(new java.io.ByteArrayInputStream(jpeg));
        assertNotNull(decoded);
        assertEquals(256, decoded.getWidth());
        assertEquals(256, decoded.getHeight());
        assertEquals(BufferedImage.OPAQUE, decoded.getTransparency());
    }

    @Test
    void normalizeToJpegBytes_opaqueJpeg_roundTrips() throws IOException {
        byte[] originalJpeg = writeJpeg(createRgbImage(512, 384));

        byte[] normalized = DeepfakeImageUtils.normalizeToJpegBytes(originalJpeg);

        assertTrue(normalized.length > 0);
        BufferedImage decoded = ImageIO.read(new java.io.ByteArrayInputStream(normalized));
        assertNotNull(decoded);
        assertEquals(512, decoded.getWidth());
        assertEquals(384, decoded.getHeight());
    }

    @Test
    void normalizeToJpegBytes_rejectsTooSmall() throws IOException {
        byte[] tinyPng = writePng(createArgbImage(64, 64));

        IOException error = assertThrows(IOException.class,
                () -> DeepfakeImageUtils.normalizeToJpegBytes(tinyPng));

        assertTrue(error.getMessage().contains(String.valueOf(DeepfakeImageUtils.MIN_DIMENSION)));
    }

    @Test
    void normalizeToJpegBytes_rejectsTooLarge() throws IOException {
        byte[] hugePng = writePng(createArgbImage(5000, 5000));

        IOException error = assertThrows(IOException.class,
                () -> DeepfakeImageUtils.normalizeToJpegBytes(hugePng));

        assertTrue(error.getMessage().contains(String.valueOf(DeepfakeImageUtils.MAX_DIMENSION)));
    }

    @Test
    void normalizeToJpegFile_writesDecodableFile() throws IOException {
        File input = tempDir.resolve("face.png").toFile();
        Files.write(input.toPath(), writePng(createArgbImage(300, 400)));

        File output = DeepfakeImageUtils.normalizeToJpegFile(input);

        assertTrue(output.exists());
        byte[] fileBytes = Files.readAllBytes(output.toPath());
        assertArrayEquals(fileBytes, DeepfakeImageUtils.normalizeToJpegBytes(Files.readAllBytes(input.toPath())));
        BufferedImage decoded = ImageIO.read(output);
        assertNotNull(decoded);
        assertEquals(300, decoded.getWidth());
        assertEquals(400, decoded.getHeight());
        output.delete();
    }

    private static BufferedImage createArgbImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, new Color(120, 80, 200, 128).getRGB());
            }
        }
        return image;
    }

    private static BufferedImage createRgbImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, new Color(40, 120, 200).getRGB());
            }
        }
        return image;
    }

    private static byte[] writePng(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private static byte[] writeJpeg(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }

    private static boolean isJpegContent(byte[] bytes) {
        return DeepfakeImageUtils.JPEG.equals(DeepfakeImageUtils.detectSupportedFaceContentType(bytes));
    }
}
