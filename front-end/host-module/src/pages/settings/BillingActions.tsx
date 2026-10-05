import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBillingActions = lazy(
  () => import('miscellaneous-module/BillingActions'),
);

const BillingActions = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBillingActions />
    </ErrorBoundaryWrapper>
  );
};

export default BillingActions;
