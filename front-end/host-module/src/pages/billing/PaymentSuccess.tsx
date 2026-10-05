import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePaymentSuccess = lazy(
  () => import('billing-module/PaymentSuccess'),
);

const PaymentSuccess = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePaymentSuccess />
    </ErrorBoundaryWrapper>
  );
};

export default PaymentSuccess;
