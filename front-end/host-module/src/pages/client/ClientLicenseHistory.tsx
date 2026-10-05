import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientLicenseHistory = lazy(
  () => import('content-module/ClientLicenseHistory'),
);

const ClientLicenseHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientLicenseHistory />
    </ErrorBoundaryWrapper>
  );
};

export default ClientLicenseHistory;
