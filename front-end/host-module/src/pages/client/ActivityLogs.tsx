import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientActivityLogs = lazy(
  () => import('user-module/ClientActivityLogs'),
);

const ClientActivityLogs = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientActivityLogs />
    </ErrorBoundaryWrapper>
  );
};

export default ClientActivityLogs;
