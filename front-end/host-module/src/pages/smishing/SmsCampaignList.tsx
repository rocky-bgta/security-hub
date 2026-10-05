import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSmsCampaignList = lazy(
  () => import('phishing-module/SmsCampaignList'),
);

const SmsCampaignList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSmsCampaignList />
    </ErrorBoundaryWrapper>
  );
};

export default SmsCampaignList;
