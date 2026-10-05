import CampaignListContent from 'features/campaign/CampaignListContent';
import { CampaignChannel } from 'models/Campaign';

const SmsCampaignList = () => (
  <CampaignListContent channel={CampaignChannel.SMS} />
);

export default SmsCampaignList;
