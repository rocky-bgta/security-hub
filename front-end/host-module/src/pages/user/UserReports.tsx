import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserReports = lazy(() => import('user-module/Reports'));

const UserReports = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserReports />
    </ErrorBoundaryWrapper>
  );
};

export default UserReports;
