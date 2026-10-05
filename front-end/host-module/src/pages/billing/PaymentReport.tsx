import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePaymentReport = lazy(() => import('billing-module/PaymentReport'));

const PaymentReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePaymentReport />
    </ErrorBoundaryWrapper>
  );
};

export default PaymentReport;
