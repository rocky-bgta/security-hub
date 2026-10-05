import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUpdatePassword = lazy(
  () => import('account-module/UpdatePassword'),
);

const UpdatePassword = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUpdatePassword />
    </ErrorBoundaryWrapper>
  );
};

export default UpdatePassword;
