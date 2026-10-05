import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserActivityReport = lazy(
  () => import('miscellaneous-module/UserActivityReport'),
);

const UserActivityReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserActivityReport />
    </ErrorBoundaryWrapper>
  );
};

export default UserActivityReport;
