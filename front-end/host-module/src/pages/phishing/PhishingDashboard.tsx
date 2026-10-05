import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePhishingDashboard = lazy(
  () => import('phishing-module/PhishingDashboard'),
);

const PhishingDashboard = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePhishingDashboard />
    </ErrorBoundaryWrapper>
  );
};

export default PhishingDashboard;
