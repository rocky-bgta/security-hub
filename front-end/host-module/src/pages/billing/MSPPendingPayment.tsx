import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPPendingPayment = lazy(
  () => import('billing-module/MSPPendingPayment'),
);

const MSPPendingPayment = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPPendingPayment />
    </ErrorBoundaryWrapper>
  );
};

export default MSPPendingPayment;
