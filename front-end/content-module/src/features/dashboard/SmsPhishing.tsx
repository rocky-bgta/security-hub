import PhishingStatisticsCard from 'features/dashboard/PhishingStatisticsCard';
import {
  IPhishingStatisticMetric,
  PhishingCampaignChannel,
} from 'models/Phishing';

const SMS_METRICS: IPhishingStatisticMetric[] = [
  { name: 'SMS Read', colors: '#1A88E0', valueKey: 'openCount' },
  { name: 'Links Clicked', colors: '#FFCE20', valueKey: 'clickCount' },
  {
    name: 'Credential Submissions',
    colors: '#F65E5B',
    valueKey: 'compromiseCount',
  },
  { name: 'Reported SMS', colors: '#2AA684', valueKey: 'reportCount' },
];

const SmsPhishing = () => (
  <PhishingStatisticsCard
    channel={PhishingCampaignChannel.SMS}
    title="Smishing Simulation Results"
    metrics={SMS_METRICS}
  />
);

export default SmsPhishing;
