import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteGlobalSettings = lazy(
  () => import('account-module/GlobalSettings'),
);

const GlobalSettings = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteGlobalSettings />
    </ErrorBoundaryWrapper>
  );
};

export default GlobalSettings;
