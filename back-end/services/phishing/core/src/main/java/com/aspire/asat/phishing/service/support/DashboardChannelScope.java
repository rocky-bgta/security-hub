package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Set;

/**
 * Maps a dashboard product channel to campaign types, activity types, and Mongo
 * campaign-channel match criteria (legacy missing/null channel counts as EMAIL).
 */
public final class DashboardChannelScope {

    public record ActivityMapping(
            ActivityType sent,
            ActivityType opened,
            ActivityType clicked,
            ActivityType hack,
            ActivityType reported) {
    }

    private DashboardChannelScope() {
    }

    public static CampaignChannel effective(CampaignChannel channel) {
        return CampaignTypeChannelValidator.effectiveChannel(channel);
    }

    public static Set<CampaignType> campaignTypes(CampaignChannel channel) {
        return Set.copyOf(switch (effective(channel)) {
            case EMAIL -> Set.of(CampaignType.SIMULATED_PHISHING, CampaignType.PHISHING_WITH_TRAINING);
            case SMS -> Set.of(CampaignType.SMISHING_SIMULATION, CampaignType.SMISHING_WITH_TRAINING);
            case VOICE -> Set.of(CampaignType.VISHING_SIMULATION, CampaignType.VISHING_WITH_TRAINING);
        });
    }

    public static ActivityMapping activityMapping(CampaignChannel channel) {
        return switch (effective(channel)) {
            case EMAIL -> new ActivityMapping(
                    ActivityType.EMAIL_SENT,
                    ActivityType.EMAIL_OPENED,
                    ActivityType.LINK_CLICKED,
                    ActivityType.DATA_SUBMITTED,
                    ActivityType.EMAIL_REPORTED);
            case SMS -> new ActivityMapping(
                    ActivityType.SMS_SENT,
                    null,
                    ActivityType.LINK_CLICKED,
                    ActivityType.DATA_SUBMITTED,
                    null);
            case VOICE -> new ActivityMapping(
                    ActivityType.VOICE_INITIATED,
                    ActivityType.VOICE_ANSWERED,
                    ActivityType.VOICE_ENGAGED,
                    ActivityType.VOICE_COMPROMISED,
                    ActivityType.VOICE_REPORTED);
        };
    }

    /**
     * EMAIL matches stored EMAIL plus legacy docs with missing/null channel.
     * SMS/VOICE match the exact enum value.
     */
    public static Criteria campaignChannelCriteria(CampaignChannel channel) {
        CampaignChannel effective = effective(channel);
        if (effective == CampaignChannel.EMAIL) {
            return new Criteria().orOperator(
                    Criteria.where("channel").is(CampaignChannel.EMAIL),
                    Criteria.where("channel").exists(false),
                    Criteria.where("channel").is(null)
            );
        }
        return Criteria.where("channel").is(effective);
    }

    /**
     * Match {@code dashboard_stats.channel} including legacy rows with no channel as EMAIL.
     */
    public static Criteria statsChannelCriteria(CampaignChannel channel) {
        return campaignChannelCriteria(channel);
    }
}
