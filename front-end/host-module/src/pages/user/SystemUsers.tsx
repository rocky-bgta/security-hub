import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSystemUsers = lazy(() => import('user-module/SystemUsers'));

const SystemUsers = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSystemUsers />
    </ErrorBoundaryWrapper>
  );
};

export default SystemUsers;
