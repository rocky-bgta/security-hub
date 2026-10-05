import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCreateSmsCampaign = lazy(
  () => import('phishing-module/CreateSmsCampaign'),
);

const CreateSmishingSimulation = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCreateSmsCampaign />
    </ErrorBoundaryWrapper>
  );
};

export default CreateSmishingSimulation;
