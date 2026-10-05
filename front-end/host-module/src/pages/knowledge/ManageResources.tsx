import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemoteManageResources = lazy(
  () => import('miscellaneous-module/ManageResources'),
);

const ManageResources = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteManageResources />
    </ErrorBoundaryWrapper>
  );
};

export default ManageResources;
