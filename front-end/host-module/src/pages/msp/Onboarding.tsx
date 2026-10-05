import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteMSPOnboarding = lazy(() => import('user-module/MspAdminOnBoarding'));

const MSPOnboarding = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteMSPOnboarding />
    </ErrorBoundaryWrapper>
  );
};

export default MSPOnboarding;
