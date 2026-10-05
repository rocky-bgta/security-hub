import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';

const RemoteMSPPaymentReportDetails = lazy(
  () => import('billing-module/MSPPaymentReportDetails'),
);

const MSPPaymentReportDetails = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPPaymentReportDetails hostPath={BillingManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default MSPPaymentReportDetails;
