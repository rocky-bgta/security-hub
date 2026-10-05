import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';

const RemotePermissionList = lazy(
  () => import('miscellaneous-module/Permissions'),
);

const PermissionList = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemotePermissionList />
    </ErrorBoundaryWrapper>
  );
};

export default PermissionList;
