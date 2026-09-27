package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RecipientResolverFactory {

    private final Map<CampaignChannel, RecipientResolver> resolvers;

    public RecipientResolverFactory(List<RecipientResolver> resolverList) {
        this.resolvers = new EnumMap<>(CampaignChannel.class);
        for (RecipientResolver resolver : resolverList) {
            resolvers.put(resolver.getChannel(), resolver);
        }
    }

    public RecipientResolver getResolver(CampaignChannel channel) {
        CampaignChannel effective = channel != null ? channel : CampaignChannel.EMAIL;
        RecipientResolver resolver = resolvers.get(effective);
        if (resolver == null) {
            throw new PhishingValidationException("No recipient resolver for channel: " + effective);
        }
        return resolver;
    }
}
