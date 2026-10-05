// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteInvoiceHistory = lazy(
//   () => import('billing-module/InvoiceHistory'),
// );

const InvoiceHistory = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteInvoiceHistory /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default InvoiceHistory;
