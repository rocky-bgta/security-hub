import CampaignListContent from 'features/campaign/CampaignListContent';
import { CampaignChannel } from 'models/Campaign';

const EmailCampaignList = () => (
  <CampaignListContent channel={CampaignChannel.EMAIL} />
);

export default EmailCampaignList;
