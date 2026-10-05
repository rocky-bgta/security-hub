import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteActivityLogs = lazy(
  () => import('user-module/ActivityLogs'),
);

const ActivityLogs = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteActivityLogs />
    </ErrorBoundaryWrapper>
  );
};

export default ActivityLogs;
