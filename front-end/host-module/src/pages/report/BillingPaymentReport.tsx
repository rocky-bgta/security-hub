import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBillingPaymentReport = lazy(
  () => import('miscellaneous-module/BillingPaymentReport'),
);

const BillingPaymentReport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBillingPaymentReport />
    </ErrorBoundaryWrapper>
  );
};

export default BillingPaymentReport;
