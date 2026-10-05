import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientAnalytics = lazy(() => import('user-module/ClientAnalytics'));

const ClientAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientAnalytics />
    </ErrorBoundaryWrapper>
  );
};

export default ClientAnalytics;
