import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAccessReport = lazy(
  () => import('miscellaneous-module/AccessReport'),
);

const AccessReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteAccessReport />
    </ErrorBoundaryWrapper>
  );
};

export default AccessReport;
