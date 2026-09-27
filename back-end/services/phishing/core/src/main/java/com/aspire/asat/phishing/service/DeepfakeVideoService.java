package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.DeepfakeStep1Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep3Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep5Request;
import com.aspire.asat.phishing.dto.request.DeepfakeStep6Request;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoDetailDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.dto.response.VideoRenderProviderDto;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DeepfakeVideoService {

    DeepfakeVideoStepResponse startStep1(DeepfakeStep1Request request);

    DeepfakeVideoStepResponse updateStep1(UUID videoId, DeepfakeStep1Request request);

    DeepfakeVideoStepResponse updateStep3(UUID videoId, DeepfakeStep3Request request);

    DeepfakeVideoStepResponse updateStep5(UUID videoId, DeepfakeStep5Request request);

    DeepfakeVideoStepResponse updateStep6(UUID videoId, DeepfakeStep6Request request);

    DeepfakeVideoDetailDto getVideoDetail(UUID videoId);

    List<DeepfakeVideoDto> listVideos(int page, int size, LocalDate uploadDate, String title, String description);

    long countVideos(LocalDate uploadDate, String title, String description);

    DeepfakeRenderJob getForEdit(UUID videoId);

    void deleteVideo(UUID videoId);

    List<VideoRenderProviderDto> listVideoRenderProviders();
}
