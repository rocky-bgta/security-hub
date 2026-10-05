import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteRequestedPolicy = lazy(
  () => import('miscellaneous-module/RequestedPolicy'),
);

const RequestedPolicy = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteRequestedPolicy />
    </ErrorBoundaryWrapper>
  );
};

export default RequestedPolicy;
