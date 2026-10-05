import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteClientSelfOnBoarding = lazy(
  () => import('user-module/ClientSelfOnBoarding'),
);

const ClientSelfOnBoarding = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientSelfOnBoarding />
    </ErrorBoundaryWrapper>
  );
};

export default ClientSelfOnBoarding;
