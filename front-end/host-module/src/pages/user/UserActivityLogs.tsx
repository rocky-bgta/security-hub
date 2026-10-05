import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserActivityLogs = lazy(
  () => import('content-module/UserActivityLogs'),
);

const UserActivityLogs = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserActivityLogs />
    </ErrorBoundaryWrapper>
  );
};

export default UserActivityLogs;
