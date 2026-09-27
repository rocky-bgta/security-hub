package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.BackgroundImageStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deepfake_images")
public class DeepfakeBackgroundImage {

    @Id
    private String id;

    @Indexed(unique = true)
    private UUID backgroundImageId;

    private String fileName;

    private String fileKey;

    /** Uploader's client admin id; null for global backgrounds. */
    @Indexed
    private String clientAdminId;

    @Indexed
    @Builder.Default
    private DeepfakeImageType imageType = DeepfakeImageType.BACKGROUND;

    @Builder.Default
    private BackgroundImageStatus status = BackgroundImageStatus.ACTIVE;

    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private boolean isGlobal = false;

    @CreatedDate
    private Instant createdAt;
}
