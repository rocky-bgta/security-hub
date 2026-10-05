import PhishingStatisticsCard from 'features/dashboard/PhishingStatisticsCard';
import {
  IPhishingStatisticMetric,
  PhishingCampaignChannel,
} from 'models/Phishing';

const VOICE_METRICS: IPhishingStatisticMetric[] = [
  { name: 'Calls Answered', colors: '#1A88E0', valueKey: 'openCount' },
  { name: 'Call Engaged', colors: '#FFCE20', valueKey: 'clickCount' },
  {
    name: 'Credential Submissions',
    colors: '#F65E5B',
    valueKey: 'compromiseCount',
  },
  { name: 'Reported Calls', colors: '#2AA684', valueKey: 'reportCount' },
];

const VoicePhishing = () => (
  <PhishingStatisticsCard
    channel={PhishingCampaignChannel.VOICE}
    title="Vishing Simulation Results"
    metrics={VOICE_METRICS}
  />
);

export default VoicePhishing;
