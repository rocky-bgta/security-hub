import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientSuspend = lazy(() => import('user-module/ClientSuspend'));

const ClientSuspend = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientSuspend />
    </ErrorBoundaryWrapper>
  );
};

export default ClientSuspend;
