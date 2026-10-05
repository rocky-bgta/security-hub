import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteCreditReason = lazy(
  () => import('miscellaneous-module/CreditReason'),
);

const CreditReason = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteCreditReason />
    </ErrorBoundaryWrapper>
  );
};

export default CreditReason;
