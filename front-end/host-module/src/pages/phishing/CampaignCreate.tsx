import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCampaignCreate = lazy(
  () => import('phishing-module/CreateCampaign'),
);

const CampaignCreate = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCampaignCreate />
    </ErrorBoundaryWrapper>
  );
};

export default CampaignCreate;
