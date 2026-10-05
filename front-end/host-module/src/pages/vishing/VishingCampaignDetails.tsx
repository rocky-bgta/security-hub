import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteVishingCampaignDetails = lazy(
  () => import('phishing-module/VishingCampaignDetails'),
);

const VishingCampaignDetails = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteVishingCampaignDetails />
    </ErrorBoundaryWrapper>
  );
};

export default VishingCampaignDetails;
