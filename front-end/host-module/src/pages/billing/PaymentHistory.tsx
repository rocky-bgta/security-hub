import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePaymentHistory = lazy(
  () => import('billing-module/PaymentHistory'),
);

const PaymentHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePaymentHistory />
    </ErrorBoundaryWrapper>
  );
};

export default PaymentHistory;
