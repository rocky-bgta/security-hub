import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';

const RemoteCreditDetails = lazy(() => import('billing-module/CreditDetails'));

const CreditDetails = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCreditDetails hostPath={BillingManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default CreditDetails;
