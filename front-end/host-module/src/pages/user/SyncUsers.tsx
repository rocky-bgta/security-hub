import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSyncUsers = lazy(() => import('user-module/SyncUsers'));

const SyncUsers = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSyncUsers />
    </ErrorBoundaryWrapper>
  );
};

export default SyncUsers;
