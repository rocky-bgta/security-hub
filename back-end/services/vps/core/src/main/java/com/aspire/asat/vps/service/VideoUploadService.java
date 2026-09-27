package com.aspire.asat.vps.service;

import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.service.files.FileService;

import com.aspire.asat.common.util.KeyStrategy;
import com.aspire.asat.vps.dto.enums.VideoStatus;
import com.aspire.asat.vps.dto.response.RequestDto;
import com.aspire.asat.vps.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoUploadService {
    private final FileService fileService;
    public ResponseDto uploadVideosToCloud(String localDirPath, RequestDto requestDto, String jobId, String currentDate) throws Exception {

        AtomicReference<String> processedVideoUrlRef = new AtomicReference<>(null);

        List<String> uploadedFiles = new ArrayList<>();
        log.info("-----------------start uploading files to cloud------------");
        try (Stream<Path> paths = Files.walk(Paths.get(localDirPath))) {
            paths.filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        String fileName = filePath.getFileName().toString();
                        String key = "asatv2/uploads/CONTENT/"+ KeyStrategy.buildKey("VIDEO",requestDto.getSpecificContentId(), fileName, false);
                        log.info("+++++++++++++++++++++++++++++++Uploading file: " + filePath + " to key: " + key);
                        if(fileName.toLowerCase().endsWith(".mpd")){
                            processedVideoUrlRef.set(key);
                        }
                        FileUploadResponse response = fileService.fileUpload(filePath.toString(), key);
                        log.info("==============================="+response.getPath());
                        uploadedFiles.add(response.getPath());

                        try {
                            Files.delete(filePath);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete local file: " + filePath, e);
                        }
                    });
        }
        String processedVideoUrl = processedVideoUrlRef.get();

        return ResponseDto.builder()
                .id(requestDto.getId())
                .specificContentId(requestDto.getSpecificContentId())
                .contentType(requestDto.getContentType())
                .processingStatus(VideoStatus.PROCESSED.toString())
                .videoUrl(requestDto.getVideoUrl())
                .processedVideoUrl(processedVideoUrl)
                .userId(requestDto.getUserId())
                .uploadedFiles(uploadedFiles)
                .uploadedAt(Instant.now())
                .build();
    }
}
