import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { routes } from 'routes/Routes';

const RemoteClientAdminLayout = lazy(
  () => import('home-module/ClientAdminLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof routes;
}

const ClientAdminLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteClientAdminLayout hostPath={hostPath}>
        {children}
      </RemoteClientAdminLayout>
    </ErrorBoundaryWrapper>
  );
};

export default ClientAdminLayout;
