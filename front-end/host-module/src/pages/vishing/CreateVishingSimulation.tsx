import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCreateVishingSimulation = lazy(
  () => import('phishing-module/CreateVishingSimulation'),
);

const CreateVishingSimulation = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCreateVishingSimulation />
    </ErrorBoundaryWrapper>
  );
};

export default CreateVishingSimulation;
