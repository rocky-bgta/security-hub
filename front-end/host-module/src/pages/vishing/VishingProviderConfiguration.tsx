import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteVishingProviderConfiguration = lazy(
  () => import('phishing-module/VishingProviderConfiguration'),
);

const VishingProviderConfiguration = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteVishingProviderConfiguration />
    </ErrorBoundaryWrapper>
  );
};

export default VishingProviderConfiguration;
