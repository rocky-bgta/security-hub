import CampaignListContent from 'features/campaign/CampaignListContent';
import { CampaignChannel } from 'models/Campaign';

const VishingCampaignList = () => (
  <CampaignListContent channel={CampaignChannel.VOICE} />
);

export default VishingCampaignList;
