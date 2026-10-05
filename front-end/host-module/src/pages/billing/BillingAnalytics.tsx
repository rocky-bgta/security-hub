import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBillingAnalytics = lazy(
  () => import('billing-module/BillingAnalytics'),
);

const BillingAnalytics = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBillingAnalytics />
    </ErrorBoundaryWrapper>
  );
};

export default BillingAnalytics;
