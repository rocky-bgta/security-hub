import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteUserOnboarding = lazy(() => import('user-module/Onboarding'));

const UserOnboarding = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserOnboarding />
    </ErrorBoundaryWrapper>
  );
};

export default UserOnboarding;
