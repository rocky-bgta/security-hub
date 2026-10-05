import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserReport = lazy(() => import('miscellaneous-module/UserReport'));

const UserReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserReport />
    </ErrorBoundaryWrapper>
  );
};

export default UserReport;
