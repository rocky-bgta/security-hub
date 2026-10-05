import { lazy, ReactNode } from 'react';

import ErrorBoundaryWrapper from 'components/error/ErrorBoundaryWrapper';
import { routes } from 'routes/Routes';

const RemoteAspireAdminLayout = lazy(
  () => import('home-module/AspireAdminLayout'),
);

interface IProps {
  children: ReactNode;
  hostPath: typeof routes;
}

const AspireAdminLayout = ({ children, hostPath }: IProps) => {
  return (
    <ErrorBoundaryWrapper loadingFallback={null}>
      <RemoteAspireAdminLayout hostPath={hostPath}>
        {children}
      </RemoteAspireAdminLayout>
    </ErrorBoundaryWrapper>
  );
};

export default AspireAdminLayout;
