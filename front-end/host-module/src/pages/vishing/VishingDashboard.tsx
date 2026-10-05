import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteVishingDashboard = lazy(
  () => import('phishing-module/VishingDashboard'),
);

const VishingDashboard = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteVishingDashboard />
    </ErrorBoundaryWrapper>
  );
};

export default VishingDashboard;
