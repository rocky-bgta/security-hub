import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { routes } from 'routes/Routes';

const RemoteSuperAdminLayout = lazy(
  () => import('home-module/SuperAdminLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof routes;
}

const SuperAdminLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteSuperAdminLayout hostPath={hostPath}>
        {children}
      </RemoteSuperAdminLayout>
    </ErrorBoundaryWrapper>
  );
};

export default SuperAdminLayout;
