import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSmishingDashboard = lazy(
  () => import('phishing-module/SmishingDashboard'),
);

const SmishingDashboard = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSmishingDashboard />
    </ErrorBoundaryWrapper>
  );
};

export default SmishingDashboard;
