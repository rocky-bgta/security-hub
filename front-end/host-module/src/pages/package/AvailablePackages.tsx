import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAvailablePackages = lazy(
  () => import('content-module/AvailablePackages'),
);

const AvailablePackages = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteAvailablePackages />
    </ErrorBoundaryWrapper>
  );
};

export default AvailablePackages;
