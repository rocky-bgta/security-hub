import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAIProviderConfiguration = lazy(
  () => import('phishing-module/AIProviderConfiguration'),
);

const AIProviderConfiguration = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteAIProviderConfiguration />
    </ErrorBoundaryWrapper>
  );
};

export default AIProviderConfiguration;
