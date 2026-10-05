import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAssignedPackages = lazy(
  () => import('content-module/AssignedPackages'),
);

const AssignedPackages = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteAssignedPackages />
    </ErrorBoundaryWrapper>
  );
};

export default AssignedPackages;
