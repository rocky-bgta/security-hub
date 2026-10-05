import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePaymentFailed = lazy(() => import('billing-module/PaymentFailed'));

const PaymentFailed = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePaymentFailed />
    </ErrorBoundaryWrapper>
  );
};

export default PaymentFailed;
