import {
  CampaignChannel,
  getCampaignChannelColor,
  getCampaignChannelLabel,
} from 'models/Campaign';
import { Mail, MessageSquare, Phone } from 'lucide-react';
import React from 'react';

interface CampaignChannelBadgeProps {
  channel?: CampaignChannel;
  size?: 'sm' | 'md';
}

export const CampaignChannelBadge: React.FC<CampaignChannelBadgeProps> = ({
  channel = CampaignChannel.EMAIL,
  size = 'md',
}) => {
  const sizeClasses = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-2.5 py-1 text-sm',
  };

  const channelIcons: Record<CampaignChannel, React.ReactNode> = {
    [CampaignChannel.EMAIL]: <Mail className="mr-1 size-3" />,
    [CampaignChannel.SMS]: <MessageSquare className="mr-1 size-3" />,
    [CampaignChannel.VOICE]: <Phone className="mr-1 size-3" />,
  };

  return (
    <span
      className={`inline-flex items-center rounded-full font-medium ${getCampaignChannelColor(channel)} ${sizeClasses[size]}`}
    >
      {channelIcons[channel]}
      {getCampaignChannelLabel(channel)}
    </span>
  );
};
