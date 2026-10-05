import { lazy } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { UserManagementRoutes } from 'routes/UserManagementRoutes';

const RemoteUserBulkImport = lazy(() => import('user-module/BulkImport'));

const UserBulkImport = () => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteUserBulkImport hostPath={UserManagementRoutes} />
    </ErrorBoundaryWrapper>
  );
};

export default UserBulkImport;
