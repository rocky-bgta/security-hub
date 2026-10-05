// import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

// const RemoteBulkExamImport = lazy(
//   () => import('account-module/BulkExamImport'),
// );

const BulkExamImport = () => {
  return (
    <ErrorBoundaryWrapper>
      {/* <RemoteBulkExamImport /> */}
      <div>Coming Soon</div>
    </ErrorBoundaryWrapper>
  );
};

export default BulkExamImport;
