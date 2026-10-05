import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';

const RemoteMSPPaymentHistory = lazy(
  () => import('billing-module/MSPPaymentHistory'),
);

const MSPPaymentHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPPaymentHistory hostPath={BillingManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default MSPPaymentHistory;
