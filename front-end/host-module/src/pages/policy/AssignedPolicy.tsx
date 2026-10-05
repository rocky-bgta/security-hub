import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteAssignedPolicy = lazy(
  () => import('miscellaneous-module/AssignedPolicy'),
);

const AssignedPolicy = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteAssignedPolicy />
    </ErrorBoundaryWrapper>
  );
};

export default AssignedPolicy;
