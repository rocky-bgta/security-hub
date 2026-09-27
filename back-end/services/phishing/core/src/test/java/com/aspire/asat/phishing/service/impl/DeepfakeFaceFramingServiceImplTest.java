package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepfakeFaceFramingServiceImplTest {

    @Mock
    private RekognitionClient rekognitionClient;

    @InjectMocks
    private DeepfakeFaceFramingServiceImpl service;

    private static byte[] jpeg(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.DARK_GRAY);
        g.fillOval(width / 3, height / 6, width / 3, height / 3);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }

    private static int[] dimensionsOf(byte[] bytes) throws Exception {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        return new int[]{image.getWidth(), image.getHeight()};
    }

    @Test
    void frameToLandscape_withDetectedFace_returns16by9() throws Exception {
        when(rekognitionClient.detectFaces(any(DetectFacesRequest.class)))
                .thenReturn(DetectFacesResponse.builder()
                        .faceDetails(FaceDetail.builder()
                                .boundingBox(BoundingBox.builder()
                                        .left(0.35f).top(0.2f).width(0.3f).height(0.35f).build())
                                .build())
                        .build());

        byte[] result = service.frameToLandscape(jpeg(900, 1200));

        assertNotNull(result);
        int[] dims = dimensionsOf(result);
        assertEquals(1920, dims[0]);
        assertEquals(1080, dims[1]);
    }

    @Test
    void frameToLandscape_noFaceDetected_fallsBackToCenterCrop() throws Exception {
        when(rekognitionClient.detectFaces(any(DetectFacesRequest.class)))
                .thenReturn(DetectFacesResponse.builder().build());

        byte[] result = service.frameToLandscape(jpeg(1000, 1000));

        int[] dims = dimensionsOf(result);
        assertEquals(1920, dims[0]);
        assertEquals(1080, dims[1]);
    }

    @Test
    void frameToLandscape_rekognitionFails_fallsBackToCenterCrop() throws Exception {
        when(rekognitionClient.detectFaces(any(DetectFacesRequest.class)))
                .thenThrow(new RuntimeException("rekognition down"));

        byte[] result = service.frameToLandscape(jpeg(1600, 900));

        int[] dims = dimensionsOf(result);
        assertEquals(1920, dims[0]);
        assertEquals(1080, dims[1]);
    }

    @Test
    void frameToLandscape_emptyInput_throwsBadRequest() {
        assertThrows(ServiceException.class, () -> service.frameToLandscape(new byte[0]));
    }
}
