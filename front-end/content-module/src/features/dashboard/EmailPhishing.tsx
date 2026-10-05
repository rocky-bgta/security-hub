import PhishingStatisticsCard from 'features/dashboard/PhishingStatisticsCard';
import {
  IPhishingStatisticMetric,
  PhishingCampaignChannel,
} from 'models/Phishing';

const EMAIL_METRICS: IPhishingStatisticMetric[] = [
  { name: 'Emails Opened', colors: '#1A88E0', valueKey: 'openCount' },
  { name: 'Links Clicked', colors: '#FFCE20', valueKey: 'clickCount' },
  {
    name: 'Credential Submissions',
    colors: '#F65E5B',
    valueKey: 'compromiseCount',
  },
  { name: 'Reported Emails', colors: '#2AA684', valueKey: 'reportCount' },
];

const EmailPhishing = () => (
  <PhishingStatisticsCard
    channel={PhishingCampaignChannel.EMAIL}
    title="Phishing Simulation Results"
    metrics={EMAIL_METRICS}
  />
);

export default EmailPhishing;
