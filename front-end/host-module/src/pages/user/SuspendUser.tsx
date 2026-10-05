import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSuspendUser = lazy(() => import('user-module/SuspendUser'));

const SuspendUser = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSuspendUser />
    </ErrorBoundaryWrapper>
  );
};

export default SuspendUser;
