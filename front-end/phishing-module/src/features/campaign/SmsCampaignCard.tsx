import {
  CampaignCardProgress,
  CampaignCardShell,
  CampaignCardStat,
  type CampaignCardProps,
} from './CampaignCardShared';

export const SmsCampaignCard = (props: CampaignCardProps) => {
  const { campaign } = props;
  const stats = campaign.stats;
  const smsSent = stats?.smsSent ?? stats?.emailsSent ?? 0;
  const progressPercent =
    stats && stats.totalRecipients > 0
      ? Math.round((smsSent / stats.totalRecipients) * 100)
      : 0;

  return (
    <CampaignCardShell {...props}>
      <div className="grid grid-cols-4 gap-2 text-center">
        <CampaignCardStat
          value={stats?.totalRecipients || 0}
          label="Recipients"
        />
        <CampaignCardStat
          value={smsSent}
          label="Sent"
          valueClassName="text-blue-600"
        />
        <CampaignCardStat
          value={stats?.linksClicked || 0}
          label="Clicked"
          valueClassName="text-orange-600"
        />
        <CampaignCardStat
          value={stats?.dataSubmitted || 0}
          label="Compromised"
          valueClassName="text-red-600"
        />
      </div>
      {stats && stats.totalRecipients > 0 && (
        <CampaignCardProgress
          label="SMS Sending Progress"
          percent={progressPercent}
        />
      )}
    </CampaignCardShell>
  );
};
