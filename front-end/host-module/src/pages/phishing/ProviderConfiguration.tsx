import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteProviderConfiguration = lazy(
  () => import('phishing-module/ProviderConfiguration'),
);

const ProviderConfiguration = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteProviderConfiguration />
    </ErrorBoundaryWrapper>
  );
};

export default ProviderConfiguration;
