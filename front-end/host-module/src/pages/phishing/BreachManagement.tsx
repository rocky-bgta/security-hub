import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBreachManagement = lazy(
  () => import('phishing-module/BreachManagement'),
);

const BreachManagement = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBreachManagement />
    </ErrorBoundaryWrapper>
  );
};

export default BreachManagement;
