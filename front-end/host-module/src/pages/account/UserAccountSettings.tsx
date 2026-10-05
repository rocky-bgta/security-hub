import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserAccountSettings = lazy(
  () => import('account-module/UserAccountSettings'),
);

const UserAccountSettings = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserAccountSettings />
    </ErrorBoundaryWrapper>
  );
};

export default UserAccountSettings;
