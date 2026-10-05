import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSecuritySettings = lazy(
  () => import('account-module/SecuritySettings'),
);

const SecuritySettings = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSecuritySettings />
    </ErrorBoundaryWrapper>
  );
};

export default SecuritySettings;
