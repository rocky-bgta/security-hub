import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { UserManagementRoutes } from 'routes/UserManagementRoutes';

const RemoteSuperAdminLayout = lazy(
  () => import('home-module/SuperAdminLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof UserManagementRoutes;
}

const SuperAdminLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteSuperAdminLayout hostPath={hostPath}>
        {children}
      </RemoteSuperAdminLayout>
    </ErrorBoundaryWrapper>
  );
};

export default SuperAdminLayout;
