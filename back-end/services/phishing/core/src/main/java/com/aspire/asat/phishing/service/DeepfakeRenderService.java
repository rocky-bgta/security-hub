package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.DeepfakeVideoStatusResponse;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.UUID;

public interface DeepfakeRenderService {

    DeepfakeVideoStepResponse enqueueRender(DeepfakeRenderJob job);

    DeepfakeVideoStatusResponse getVideoStatus(UUID videoId);

    /**
     * @return true when the SQS message can be safely deleted (processed or benign duplicate)
     */
    boolean processRender(Message message);
}
