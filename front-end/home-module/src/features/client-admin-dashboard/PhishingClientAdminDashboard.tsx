import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePhishingClientAdminDashboard = lazy(
  () => import('phishing-module/PhishingDashboard'),
);

const PhishingClientAdminDashboard = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePhishingClientAdminDashboard />
    </ErrorBoundaryWrapper>
  );
};

export default PhishingClientAdminDashboard;
