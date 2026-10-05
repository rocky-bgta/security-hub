import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteStates = lazy(() => import('miscellaneous-module/States'));

const States = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteStates />
    </ErrorBoundaryWrapper>
  );
};

export default States;
