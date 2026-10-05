import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCampaignList = lazy(() => import('phishing-module/CampaignList'));

const CampaignList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCampaignList />
    </ErrorBoundaryWrapper>
  );
};

export default CampaignList;
