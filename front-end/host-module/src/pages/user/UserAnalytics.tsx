import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserAnalytics = lazy(() => import('user-module/Analytics'));

const UserAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserAnalytics />
    </ErrorBoundaryWrapper>
  );
};

export default UserAnalytics;
