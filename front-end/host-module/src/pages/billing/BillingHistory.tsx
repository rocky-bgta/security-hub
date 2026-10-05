import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBillingHistory = lazy(
  () => import('billing-module/BillingHistory'),
);

const BillingHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBillingHistory />
    </ErrorBoundaryWrapper>
  );
};

export default BillingHistory;
