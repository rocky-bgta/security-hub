import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { BillingManagementRoutes } from 'routes/BillingManagementRoutes';
import { UserManagementRoutes } from 'routes/UserManagementRoutes';

const RemoteClientAdminLayout = lazy(
  () => import('home-module/ClientAdminLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof UserManagementRoutes | typeof BillingManagementRoutes;
}

const ClientAdminLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteClientAdminLayout hostPath={hostPath}>
        {children}
      </RemoteClientAdminLayout>
    </ErrorBoundaryWrapper>
  );
};

export default ClientAdminLayout;
