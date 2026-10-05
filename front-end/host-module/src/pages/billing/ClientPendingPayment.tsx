import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientPendingPayment = lazy(
  () => import('billing-module/ClientPendingPayment'),
);

const ClientPendingPayment = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientPendingPayment />
    </ErrorBoundaryWrapper>
  );
};

export default ClientPendingPayment;
