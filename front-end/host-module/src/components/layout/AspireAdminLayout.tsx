import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { ClientManagementRoutes } from 'routes/ClientManagementRoutes';

const RemoteAspireAdminLayout = lazy(
  () => import('home-module/AspireAdminLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof ClientManagementRoutes;
}

const AspireAdminLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper>
      <RemoteAspireAdminLayout hostPath={hostPath}>
        {children}
      </RemoteAspireAdminLayout>
    </ErrorBoundaryWrapper>
  );
};

export default AspireAdminLayout;
