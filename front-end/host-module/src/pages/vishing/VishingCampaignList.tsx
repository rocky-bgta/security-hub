import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteVishingCampaignList = lazy(
  () => import('phishing-module/VishingCampaignList'),
);

const VishingCampaignList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteVishingCampaignList />
    </ErrorBoundaryWrapper>
  );
};

export default VishingCampaignList;
