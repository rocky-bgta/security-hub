import { CampaignChannel } from 'models/Campaign';
import { EmailCampaignCard } from './EmailCampaignCard';
import { SmsCampaignCard } from './SmsCampaignCard';
import { VishingCampaignCard } from './VishingCampaignCard';
import type { CampaignCardProps } from './CampaignCardShared';

export type { CampaignCardProps };

export const CampaignCard = (props: CampaignCardProps) => {
  switch (props.campaign.channel) {
    case CampaignChannel.SMS:
      return <SmsCampaignCard {...props} />;
    case CampaignChannel.VOICE:
      return <VishingCampaignCard {...props} />;
    default:
      return <EmailCampaignCard {...props} />;
  }
};
