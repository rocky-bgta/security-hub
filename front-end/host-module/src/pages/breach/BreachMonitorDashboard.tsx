import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBreachMonitorDashboard = lazy(
  () => import('phishing-module/BreachMonitorDashboard'),
);

const BreachMonitorDashboard = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBreachMonitorDashboard />
    </ErrorBoundaryWrapper>
  );
};

export default BreachMonitorDashboard;
