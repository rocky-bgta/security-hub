import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientPaymentHistory = lazy(
  () => import('billing-module/ClientPaymentHistory'),
);

const ClientPaymentHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientPaymentHistory />
    </ErrorBoundaryWrapper>
  );
};

export default ClientPaymentHistory;
