import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientManageLicense = lazy(
  () => import('billing-module/ClientManageLicense'),
);

const ClientManageLicense = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientManageLicense />
    </ErrorBoundaryWrapper>
  );
};

export default ClientManageLicense;
