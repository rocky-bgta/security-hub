import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBreachDashboard = lazy(
  () => import('phishing-module/BreachDashboard'),
);

const BreachDashboard = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBreachDashboard />
    </ErrorBoundaryWrapper>
  );
};

export default BreachDashboard;
