import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientBillingReport = lazy(
  () => import('billing-module/ClientBillingReport'),
);

const ClientBillingReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientBillingReport />
    </ErrorBoundaryWrapper>
  );
};

export default ClientBillingReport;
