package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.enums.TemplateType;

public final class PayloadTypeChannelSupport {

    private PayloadTypeChannelSupport() {
    }

    public static PayloadTypeChannel effectiveChannel(PayloadTypeChannel channel) {
        return channel != null ? channel : PayloadTypeChannel.EMAIL;
    }

    public static PayloadTypeChannel effectiveChannelForUpdate(PayloadTypeChannel requestedChannel,
                                                               PayloadTypeChannel existingChannel) {
        if (requestedChannel != null) {
            return requestedChannel;
        }
        return effectiveChannelFromEntity(existingChannel);
    }

    public static PayloadTypeChannel effectiveChannelFromEntity(PayloadTypeChannel channel) {
        return channel != null ? channel : PayloadTypeChannel.EMAIL;
    }

    public static PayloadTypeChannel fromTemplateType(TemplateType templateType) {
        return templateType == TemplateType.SMS ? PayloadTypeChannel.SMS : PayloadTypeChannel.EMAIL;
    }
}
