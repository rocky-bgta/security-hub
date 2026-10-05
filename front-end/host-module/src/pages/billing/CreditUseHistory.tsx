import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';

const RemoteCreditUseHistory = lazy(
  () => import('billing-module/CreditUseHistory'),
);

const CreditUseHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCreditUseHistory hostPath={BillingManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default CreditUseHistory;
