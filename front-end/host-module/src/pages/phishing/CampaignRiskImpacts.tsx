import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCampaignRiskImpacts = lazy(
  () => import('phishing-module/CampaignRiskImpacts'),
);

type Channel = 'phishing' | 'smishing' | 'vishing';

const CampaignRiskImpacts = ({ channel }: { channel?: Channel }) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCampaignRiskImpacts channel={channel} />
    </ErrorBoundaryWrapper>
  );
};

export default CampaignRiskImpacts;
