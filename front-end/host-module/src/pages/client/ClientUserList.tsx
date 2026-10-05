import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientUserList = lazy(() => import('user-module/ClientUserList'));

const ClientUserList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientUserList />
    </ErrorBoundaryWrapper>
  );
};

export default ClientUserList;
