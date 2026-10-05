import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSuspendReason = lazy(
  () => import('miscellaneous-module/SuspendReason'),
);

const SuspendReason = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSuspendReason />
    </ErrorBoundaryWrapper>
  );
};

export default SuspendReason;
