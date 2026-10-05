import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSmsCampaignDetails = lazy(
  () => import('phishing-module/SmsCampaignDetails'),
);

const SmsCampaignDetails = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSmsCampaignDetails />
    </ErrorBoundaryWrapper>
  );
};

export default SmsCampaignDetails;
