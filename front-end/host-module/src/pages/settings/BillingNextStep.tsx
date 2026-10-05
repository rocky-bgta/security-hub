import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteBillingNextStep = lazy(
  () => import('miscellaneous-module/BillingNextStep'),
);

const BillingNextStep = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteBillingNextStep />
    </ErrorBoundaryWrapper>
  );
};

export default BillingNextStep;
