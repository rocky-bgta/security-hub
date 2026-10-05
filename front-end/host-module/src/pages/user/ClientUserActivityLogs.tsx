import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientUserActivityLogs = lazy(
  () => import('user-module/ClientUserActivityLogs'),
);

const ClientUserActivityLogs = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientUserActivityLogs />
    </ErrorBoundaryWrapper>
  );
};

export default ClientUserActivityLogs;
