import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';

const RemoteCreditList = lazy(() => import('billing-module/CreditList'));

const CreditList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCreditList hostPath={BillingManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default CreditList;
