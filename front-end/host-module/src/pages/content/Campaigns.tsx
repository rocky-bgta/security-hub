import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCampaigns = lazy(() => import('content-module/Campaigns'));

const Campaigns = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCampaigns />
    </ErrorBoundaryWrapper>
  );
};

export default Campaigns;
