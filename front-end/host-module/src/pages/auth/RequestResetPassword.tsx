import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteRequestResetPassword = lazy(
  () => import('home-module/RequestResetPassword'),
);

const RequestResetPassword = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteRequestResetPassword />
    </ErrorBoundaryWrapper>
  );
};

export default RequestResetPassword;
