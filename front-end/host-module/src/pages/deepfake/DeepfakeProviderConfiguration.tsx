import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteDeepfakeProviderConfiguration = lazy(
  () => import('phishing-module/DeepfakeProviderConfiguration'),
);

const DeepfakeProviderConfiguration = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteDeepfakeProviderConfiguration />
    </ErrorBoundaryWrapper>
  );
};

export default DeepfakeProviderConfiguration;
