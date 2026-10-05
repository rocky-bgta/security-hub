import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUsers = lazy(() => import('user-module/Users'));

const Users = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUsers />
    </ErrorBoundaryWrapper>
  );
};

export default Users;
