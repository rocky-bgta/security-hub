import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteResetPassword = lazy(() => import('home-module/ResetPassword'));

const ResetPassword = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteResetPassword />
    </ErrorBoundaryWrapper>
  );
};

export default ResetPassword;
