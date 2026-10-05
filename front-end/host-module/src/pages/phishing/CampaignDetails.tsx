import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCampaignDetails = lazy(() => import('phishing-module/CampaignDetails'));

const CampaignDetails = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCampaignDetails />
    </ErrorBoundaryWrapper>
  );
};

export default CampaignDetails;
