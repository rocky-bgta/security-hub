import {
  CampaignCardProgress,
  CampaignCardShell,
  CampaignCardStat,
  type CampaignCardProps,
} from './CampaignCardShared';

export const EmailCampaignCard = (props: CampaignCardProps) => {
  const { campaign } = props;
  const stats = campaign.stats;
  const emailsSent = stats?.emailsSent ?? 0;
  const progressPercent =
    stats && stats.totalRecipients > 0
      ? Math.round((emailsSent / stats.totalRecipients) * 100)
      : 0;

  return (
    <CampaignCardShell {...props}>
      <div className="grid grid-cols-4 gap-2 text-center">
        <CampaignCardStat
          value={stats?.totalRecipients || 0}
          label="Recipients"
        />
        <CampaignCardStat
          value={stats?.emailsOpened || 0}
          label="Opened"
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
          label="Emails Sending Progress"
          percent={progressPercent}
        />
      )}
    </CampaignCardShell>
  );
};
