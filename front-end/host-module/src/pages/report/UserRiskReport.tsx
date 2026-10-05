import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserRiskReport = lazy(
  () => import('miscellaneous-module/UserRiskReport'),
);

const UserRiskReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserRiskReport />
    </ErrorBoundaryWrapper>
  );
};

export default UserRiskReport;
