import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientReports = lazy(() => import('user-module/ClientReports'));

const ClientReports = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientReports />
    </ErrorBoundaryWrapper>
  );
};

export default ClientReports;
