import {
  CampaignCardProgress,
  CampaignCardShell,
  CampaignCardStat,
  type CampaignCardProps,
} from './CampaignCardShared';

export const VishingCampaignCard = (props: CampaignCardProps) => {
  const { campaign } = props;
  const stats = campaign.stats;
  const callsAnswered = stats?.callsAnswered ?? 0;
  const progressPercent =
    stats && stats.totalRecipients > 0
      ? Math.round((callsAnswered / stats.totalRecipients) * 100)
      : 0;

  return (
    <CampaignCardShell {...props}>
      <div className="grid grid-cols-4 gap-2 text-center">
        <CampaignCardStat
          value={stats?.totalRecipients || 0}
          label="Recipients"
        />
        <CampaignCardStat
          value={callsAnswered}
          label="Answered"
          valueClassName="text-blue-600"
        />
        <CampaignCardStat
          value={stats?.callsCompromised || 0}
          label="Compromised"
          valueClassName="text-red-600"
        />
        <CampaignCardStat
          value={stats?.callsFailed || 0}
          label="Failed"
          valueClassName="text-red-400"
        />
      </div>
      {stats && stats.totalRecipients > 0 && (
        <CampaignCardProgress label="Call Progress" percent={progressPercent} />
      )}
    </CampaignCardShell>
  );
};
