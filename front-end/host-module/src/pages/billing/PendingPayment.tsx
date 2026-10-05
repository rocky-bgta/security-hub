import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePendingPayment = lazy(
  () => import('billing-module/PendingPayment'),
);

const PendingPayment = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePendingPayment />
    </ErrorBoundaryWrapper>
  );
};

export default PendingPayment;
