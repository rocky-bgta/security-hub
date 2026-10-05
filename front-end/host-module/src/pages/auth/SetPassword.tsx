import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteSetPassword = lazy(() => import('home-module/SetPassword'));

const SetPassword = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSetPassword />
    </ErrorBoundaryWrapper>
  );
};

export default SetPassword;
