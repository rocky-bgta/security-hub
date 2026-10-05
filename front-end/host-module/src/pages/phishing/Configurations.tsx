import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteConfigurations = lazy(() => import('phishing-module/Configurations'));

const Configurations = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteConfigurations />
    </ErrorBoundaryWrapper>
  );
};

export default Configurations;
